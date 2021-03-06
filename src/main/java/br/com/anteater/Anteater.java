package br.com.anteater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import br.com.anteater.builder.AntBuilder;
import br.com.anteater.builder.MissingMethodException;
import br.com.anteater.builder.nested.NestedContainer;
import br.com.anteater.builder.nested.NestedLambda;
import br.com.anteater.builder.nested.NestedParams;
import br.com.anteater.script.ScriptLoader;
import br.com.anteater.tasks.AnteaterTasks;
import br.com.anteater.tasks.TaskResult;

public class Anteater {

	private AntBuilder ant = new AntBuilder();
	AnteaterTasks extendTasks;

	public Anteater(ScriptLoader loader) {
		super();
		Project project = ant.getAntProject();
		project.setName("default");
		this.extendTasks = new AnteaterTasks(project, loader);
	}

	public Object exec(Context cx, Scriptable thisObj, Object[] functionParams, Function funObj) throws MissingMethodException, InvalidArgumentsException {
		String methodName = (String) functionParams[0];
		NativeArray args = (NativeArray) functionParams[1];
		TaskResult ret = extendTasks.execute(cx, thisObj, methodName, args);
		return ret.isExecuted() ? ret.getResult() : executeDefaultTask(cx, thisObj, methodName, args);
	}

	private Object executeDefaultTask(Context cx, Scriptable thisObj, String methodName, NativeArray args) throws InvalidArgumentsException, MissingMethodException {
		NestedContainer container = new NestedContainer();
		ArrayList<Object> arguments = new ArrayList<Object>();
		HashMap<String, Object> params = new HashMap<String, Object>();
		arguments.add(params);

		String paramText = null;
		NestedLambda method = null;

		if (args.size() > 4) {
			throw new InvalidArgumentsException("Number of params don\'t supported.");
		}

		for (Object arg : args) {
			if (arg instanceof NativeObject) {
				getParams(thisObj, (Scriptable) arg, params, container);
			} else if (arg instanceof String) {
				if (paramText != null) {
					throw new InvalidArgumentsException("Two params text don\'t supported.");
				}

				paramText = (String) arg;
				arguments.add(paramText);
			} else if (arg instanceof Function) {

				if (method != null) {
					throw new InvalidArgumentsException("Two lambda functions don\'t supported.");
				}
				method = new NestedLambda(cx, thisObj, (Function) arg);
				container.addObject(method);// Adicionado no container, pois
											// terá mais NestedObject
			} else {
				throw new InvalidArgumentsException("Type of param don't supported.");
			}
		}
		arguments.add(container);
		ant.invokeMethod(methodName, arguments.toArray());
		return null;
	}

	private void getScriptableParams(Scriptable thisObj, String myName, Scriptable object, NestedContainer parent) {
		Map<String, Object> params = new HashMap<String, Object>();
		NestedParams thisNested = new NestedParams(myName, params, ant);
		parent.addObject(thisNested);

		getParams(thisObj, object, params, thisNested);
	}

	private void getParams(Scriptable thisObj, Scriptable object, Map<String, Object> params, NestedContainer thisNested) {
		for (Object objName : object.getIds()) {
			String name = (String) objName;
			Object child = object.get(name, thisObj);
			if (child instanceof NativeObject) {
				getScriptableParams(thisObj, name, (Scriptable) child, thisNested);
				/*
				 * This permit the call of several times of same method
				 * (include,include ...)
				 */
			} else if (child instanceof NativeArray) {
				NativeArray array = (NativeArray) child;
				for (Object obj : array) {
					getScriptableParams(thisObj, name, (Scriptable) obj, thisNested);
				}
			} else {
				params.put(name, child);
			}
		}
	}

	public void endExecution() {

	}

	public String getDefaultTarget() {
		return extendTasks.getDefaultTarget();
	}

	public void executeTarget(Context cx, String defaultTarget) {
		extendTasks.executeTarget(cx, defaultTarget);
	}

}
