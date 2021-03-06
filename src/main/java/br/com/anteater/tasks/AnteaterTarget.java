package br.com.anteater.tasks;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import br.com.anteater.InvalidArgumentsException;

/**
 * Anteater target is only "special" lambda functions
 * 
 * @author Renato.Lorenzi
 * 
 */
public class AnteaterTarget {
	private Scriptable thisObj;
	private String name;
	private Function lambda;
	private String[] depends;

	public AnteaterTarget(String projectName, Scriptable thisObj, NativeArray args) throws InvalidArgumentsException {
		super();
		this.thisObj = thisObj;
		String dependsStr;
		try {
			// Can be NativeObject
			Checks.checkArgs(args, 2, NativeObject.class, Function.class);
			NativeObject params = (NativeObject) args.get(0);
			Checks.checkArgs(params, new String[] { "name" }, String.class);
			name = (String) params.get("name");
			dependsStr = (String) params.get("depends");
		} catch (InvalidArgumentsException e) {
			// if it is not native object is String (only name)
			Checks.checkArgs(args, 2, String.class, Function.class);
			name = (String) args.get(0);
			dependsStr = null;
		}
		this.lambda = (Function) args.get(1);

		depends = dependsStr != null && !dependsStr.equals("") ? dependsStr.split(",") : new String[] {};

		name = projectName == null ? name : projectName + "." + name;
	}

	public Scriptable getThisObj() {
		return thisObj;
	}

	public void setThisObj(Scriptable thisObj) {
		this.thisObj = thisObj;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Function getLambda() {
		return lambda;
	}

	public void setLambda(Function lambda) {
		this.lambda = lambda;
	}

	public String[] getDepends() {
		return depends;
	}
}
