/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.proteome.util.reflection;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import net.sf.cglib.reflect.FastClass;

/**
 * A set of utility methods to make reflection easier by encapsulating all
 * exceptions into {@link ReflectionUncheckedException}s and using CGLIB to
 * improve reflection performance. Since it is an unchecked exception you do not
 * need to catch it. It is declared as a throw to let javadoc document that it
 * is thrown.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ReflectionUtils {

	/**
	 * The int returned from index methods in this class when the queried for
	 * method could not be found
	 */
	public static final int INVALID_METHOD_INDEX = -1;

	private static final Object[] EMPTY_OBJECT_ARRAY = ArrayUtils.EMPTY_OBJECT_ARRAY;

	/**
	 * Takes in a class, extracts the package & returns a String which can be
	 * used in the {@link Class} resource methods (with the addition of the
	 * target file). Output string will look like <strong>/uk/ac/tmp.txt</strong>
	 *
	 * @param clazz
	 *            A class which resides in the same package
	 * @param resourceName
	 *            The name of the resource
	 * @return A String which can be used in
	 *         {@link Class#getResourceAsStream(String)}
	 */
	public static String getResourceAsStreamCompatibleName(Class<?> clazz,
			String resourceName) {
		String output = "/" + clazz.getPackage().getName();
		output = output.replaceAll("\\.", "/");
		return output + "/" + resourceName;
	}

	/**
	 * Wrapper for the {@link Class#forName(String)} but wraps exceptions in the
	 * runtime exception and will throw one if the generated class was null; I'm
	 * not even sure if this is a possible return from
	 * {@link Class#forName(String)}
	 */
	public static Class<?> getClassFromName(String className)
			throws ReflectionUncheckedException {
		try {
			Class<?> generatedClass = Class.forName(className);
			if (generatedClass == null) {
				throw new ReflectionUncheckedException(
						"Generated class was null for " + className);
			} else {
				return generatedClass;
			}
		} catch (ClassNotFoundException e) {
			throw new ReflectionUncheckedException(
					"Could not find class for name " + className, e);
		}
	}

	/**
	 * Wrapper for {@link #getClassFromName(String)} but with the added type
	 * check where it will throw an exception if the generated class is not
	 * assignable to the given expected class.
	 *
	 * @param className
	 *            The class name to generate the class from
	 * @param expected
	 *            The expected class type to recieve
	 * @return The generated class
	 * @throws ReflectionUncheckedException
	 *             Can be passed up from the wrapper method call or if the
	 *             generated class could not be assigned to the given expected
	 *             class
	 */
	public static Class<?> getClassFromName(String className, Class<?> expected)
			throws ReflectionUncheckedException {
		Class<?> generatedClass = getClassFromName(className);
		if (!expected.isAssignableFrom(generatedClass)) {
			throw new ReflectionUncheckedException("The generated class "
					+ generatedClass
					+ " was not assignable to the expected class type "
					+ expected);
		}
		return generatedClass;
	}

	/**
	 * Wrapper for {@link #newInstance(String, Object[])} where params are empty
	 * arrays with the exception of className
	 */
	public static Object newInstance(String className)
			throws ReflectionUncheckedException {
		return newInstance(className, EMPTY_OBJECT_ARRAY);
	}

	/**
	 * Wrapper for {@link #newInstance(Class, Object[])} but giving it empty
	 * Class and Object arrays
	 */
	public static Object newInstance(Class<?> target)
			throws ReflectionUncheckedException {
		return newInstance(target, EMPTY_OBJECT_ARRAY);
	}

	/**
	 * Wrapper for {@link #newInstance(Class, Object[])} but runs
	 * {@link #getClassFromName(String)}
	 */
	public static Object newInstance(String className, Object[] args)
			throws ReflectionUncheckedException {
		Class<?> targetClass = getClassFromName(className);
		return newInstance(targetClass, args);
	}

	/**
	 * Runs {@link #newInstance(Class, Class[], Object[])} but constructs the
	 * types array from the input arguments. You may not want to do this if your
	 * constructor uses an interface or superclass as a constructor argument
	 */
	public static Object newInstance(Class<?> target, Object[] args)
			throws ReflectionUncheckedException {
		Class<?>[] types = generateClassArrayFromObject(args);
		return newInstance(target, types, args);
	}

	/**
	 * Wrapper for {@link #newInstance(Class, Class[], Object[])} but first
	 * attempts to call {@link #getClassFromName(String)}
	 */
	public static Object newInstance(String className, Class<?>[] types,
			Object[] args) throws ReflectionUncheckedException {
		Class<?> targetClass = getClassFromName(className);
		return newInstance(targetClass, types, args);
	}

	/**
	 * Creates an instance of the given class with the argument types and values
	 * using CGLIB's {@link FastClass}
	 */
	public static Object newInstance(Class<?> target, Class<?>[] types,
			Object[] args) throws ReflectionUncheckedException {
		Object createdObject = null;

		try {
			createdObject = target.getConstructor(types).newInstance(args);
		} catch (IllegalArgumentException e) {
			throwInstantiationException("Exception detected", target, types,
					args, e);
		} catch (SecurityException e) {
			throwInstantiationException("Exception detected", target, types,
					args, e);
		} catch (InstantiationException e) {
			throwInstantiationException("Exception detected", target, types,
					args, e);
		} catch (IllegalAccessException e) {
			throwInstantiationException("Exception detected", target, types,
					args, e);
		} catch (NoSuchMethodException e) {
			throwInstantiationException("Exception detected", target, types,
					args, e);
		} catch (InvocationTargetException e) {
			throwInstantiationException("Exception detected", target, types,
					args, e);
		}

		if (createdObject == null) {
			throwInstantiationException("Created object was null", target,
					types, args);
		}

		return createdObject;
	}

	/**
	 * Generates a Class[] from the input Object[]
	 */
	private static Class<?>[] generateClassArrayFromObject(Object[] args) {
		Class<?>[] output = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			output[i] = args[i].getClass();
		}
		return output;
	}

	private static void throwInstantiationException(String reason,
			Class<?> target, Class<?>[] types, Object[] args) {
		throwInstantiationException(reason, target, types, null);
	}

	private static void throwInstantiationException(String reason,
			Class<?> target, Class<?>[] types, Object[] args, Exception e) {
		String msg = MessageFormat
				.format(
						"Could not instantiate instance of {0} with class types {1} and values {2}. Reason: {3}",
						new Object[] { target.getName(),
								Arrays.toString(types), Arrays.toString(args),
								reason });
		if (e == null) {
			throw new ReflectionUncheckedException(msg);
		} else {
			throw new ReflectionUncheckedException(msg, e);
		}
	}

	/**
	 * Returns the CGLIB fast class which is the root of CGLIB's improved
	 * reflection performance. From this you can access all other Fast members
	 */
	public static FastClass getFastClass(Class<?> target) {
		return FastClass.create(target.getClassLoader(), target);
	}

	/**
	 * Wrapper for {@link #getFastMethodIndex(FastClass, Method)}
	 */
	public static int getFastMethodIndex(Class<?> target, Method method) {
		return getFastMethodIndex(getFastClass(target), method);
	}

	/**
	 * Returns the index of the method in the given fast class. Provides a
	 * faster way to invoke the method than referencing using FastMethod
	 */
	public static int getFastMethodIndex(FastClass target, Method method) {
		return target.getIndex(method.getName(), method.getParameterTypes());
	}

	/**
	 * Uses fast class to find a matching method signature matching the one
	 * specified
	 */
	public static int getFastMethodIndex(FastClass target, String methodName,
			Class<?>[] parameterTypes) {
		int index = target.getIndex(methodName, parameterTypes);
		return index;
	}

	/**
	 * Wrapper for {@link #hasMethod(FastClass, Method)}
	 */
	public static boolean hasMethod(Class<?> target, Method method) {
		return hasMethod(getFastClass(target), method);
	}

	/**
	 * Returns true if a method matching the given method was found in this
	 * class
	 */
	public static boolean hasMethod(FastClass target, Method method) {
		int index = getFastMethodIndex(target, method);
		return (index != INVALID_METHOD_INDEX);
	}

	/**
	 * Used to invoke a static method with no associated parameters
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(String className, String methodName) {
		Class<?> clazz = getClassFromName(className);
		return (T) invokeMethod(clazz, methodName);
	}

	/**
	 * Used to invoke a static method with no associated parameters
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Class<?> clazz, String methodName) {
		Object obj = null;
		FastClass fastClass = getFastClass(clazz);
		Object[] params = EMPTY_OBJECT_ARRAY;
		Class<?>[] argumentTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
		return (T) invokeMethod(obj, fastClass, methodName, params,
				argumentTypes);
	}

	/**
	 * Used as a way of easily invoking methods with no parameters
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object obj, String methodName) {
		return (T) invokeMethod(obj, methodName, EMPTY_OBJECT_ARRAY);
	}

	/**
	 * Invokes a method with types set to the class types of the params array.
	 * This is fine so long as you are not attempting to reflect for a method
	 * which uses interfaces or superclasses as its signature.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object obj, String methodName,
			Object[] params) {
		Class<?>[] argumentTypes = generateClassArrayFromObject(params);
		return (T) invokeMethod(obj, methodName, params, argumentTypes);
	}

	/**
	 * Used as a final shortcut into the fast class defining method
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object obj, String methodName,
			Object[] params, Class<?>[] argumentTypes) {
		if (obj == null) {
			throw new ReflectionUncheckedException(
					"Cannot invoke a method on a " + "null object");
		}
		FastClass clazz = getFastClass(obj.getClass());

		return (T) invokeMethod(obj, clazz, methodName, params, argumentTypes);
	}

	/**
	 * Reflects into the class & runs the given method.
	 *
	 * @param <T>
	 *            The required object output type
	 * @param obj
	 *            The object to attempt to invoke the method on
	 * @param methodName
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object obj, FastClass clazz,
			String methodName, Object[] params, Class[] argumentTypes) {
		T output = null;
		int methodIndex = getFastMethodIndex(clazz, methodName, argumentTypes);

		if (methodIndex == INVALID_METHOD_INDEX) {
			String typesString = Arrays.toString(argumentTypes);
			throw new ReflectionUncheckedException("Could not find method for "
					+ methodName + " " + typesString + " in class "
					+ obj.getClass());
		}

		try {
			output = (T) clazz.invoke(methodIndex, obj, params);
		} catch (InvocationTargetException e) {
			String className = obj.getClass().getName();
			String paramsString = Arrays.toString(params);
			String typesString = Arrays.toString(argumentTypes);
			throw new ReflectionUncheckedException(
					"Cannot invoke method in target " + className
							+ " with params " + paramsString + " and types "
							+ typesString, e);
		}

		return output;
	}

	/**
	 * A method for reflecting into the constructors
	 *
	 * @param c
	 *            The target class to search against
	 * @param param
	 *            The constructor signiture to look for with respect to order
	 * @return A boolean indicating if the requried constructor was found
	 */
	public static boolean hasConstructor(Class<?> c, Class<?>[] param) {
		boolean found = false;
		for (Constructor<?> con : c.getConstructors()) {
			Class<?>[] pTypes = con.getParameterTypes();
			if (param.length == pTypes.length) {
				found = true;
				for (int i = 0; i < pTypes.length; ++i) {
					if (!pTypes[i].equals(param[i])) {
						found = false;
						break;
					}
				}
			}
			if (found) {
				break;
			}
		}
		return found;
	}

	/**
	 * Small piece of logic which will cause a runtime exception to be thrown if
	 * the given class was not an interface.
	 */
	public static void assertClassIsInterface(Class<?> assumedInterface)
			throws ReflectionUncheckedException {
		if (!assumedInterface.isInterface()) {
			throw new ReflectionUncheckedException("The given class must be "
					+ "an interface: " + assumedInterface.getName());
		}
	}

	/**
	 * Causes an exception to be thrown if the given object's class cannot be
	 * assigned to a given target class
	 */
	public static void assertObjectImplementsClass(Class<?> targetClass,
			Object object) throws ReflectionUncheckedException {
		Class<?> sourceClass = object.getClass();
		if (!targetClass.isAssignableFrom(sourceClass)) {
			String sourceClassName = sourceClass.getName();
			String targetClassName = targetClass.getName();
			throw new ReflectionUncheckedException("The given class "
					+ sourceClassName + " cannot be assigned to "
					+ targetClassName);
		}
	}

	/**
	 * Wrapper for {@link #getFieldsOfSpecifiedTypeFromObject(Class, Class[])
	 * when only one type of object is wanted.
	 */
	public static List<Field> getFieldsOfSpecifiedTypeFromObject(
			Class<?> targetclass, Class<?> objectType) {
		return getFieldsOfSpecifiedTypeFromObject(targetclass,
				new Class[] { objectType });
	}

	/**
	 * For the given target class this method will find all fields whose class
	 * type is assignable from the given array of compatable object types
	 *
	 * @param targetclass
	 *            The class you want to search through
	 * @param objectTypes
	 *            The object types you want to search for
	 * @return A list of fields from the given object which map back to the
	 *         specified objectType
	 */
	public static List<Field> getFieldsOfSpecifiedTypeFromObject(
			Class<?> targetclass, Class<?>[] objectTypes) {
		List<Field> fields = new ArrayList<Field>();
		for (Field field : targetclass.getDeclaredFields()) {
			for (Class<?> objectType : objectTypes) {
				if (objectType.isAssignableFrom(field.getType())) {
					fields.add(field);
					break;
				}
			}
		}
		return fields;
	}

	/**
	 * For a given package name this class which attempt to find all classes in
	 * that package. Currently does not work with java classes
	 *
	 * @param packageName
	 *            The package to search of the form package.name
	 * @return An array of all classes found at this position
	 * @throws ReflectionUncheckedException
	 */
	@Deprecated
	public static Class<?>[] getClassesFromPackage(String packageName)
			throws ReflectionUncheckedException {

		String convertedPackageName = packageName.replace('.', '/');

		// URL urlResource =
		// ReflectionUtils.class.getResource(convertedPackageName);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL urlResource = loader.getResource(convertedPackageName);
		if (urlResource == null) {
			throw new ReflectionUncheckedException("Could not find resource "
					+ packageName);
		}

		ArrayList<Class<?>> outputClasses = new ArrayList<Class<?>>();

		File directory = new File(urlResource.getFile());
		if (directory.exists()) {

			File[] files = directory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.endsWith(".class"));
				}
			});

			if (files.length == 0) {
				throw new ReflectionUncheckedException(
						"Could not find any classes in the given package "
								+ packageName);
			}

			for (File currentFile : files) {
				String className = StringUtils.substringAfter(currentFile
						.getPath(), convertedPackageName + '/');
				className = StringUtils.substringBefore(className, ".class");
				className = packageName + '.' + className;
				Class<?> clazz = getClassFromName(className);
				outputClasses.add(clazz);
			}
		} else {
			throw new ReflectionUncheckedException("Could not find directory "
					+ directory);
		}

		return outputClasses.toArray(new Class[outputClasses.size()]);
	}

	/**
	 * Retrieve the value of a named field for a given object
	 *
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object obj, String fieldName) {
		Field f = null;
		try {
			f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(obj);
		} catch (SecurityException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} catch (NoSuchFieldException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} finally {
			if (f != null) {
				f.setAccessible(false);
			}
		}
	}

	public static <T> Object getFieldValue(T obj, Class<T> c, String fieldName) {
		Field f = null;
		try {
			f = c.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(obj);
		} catch (SecurityException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} catch (NoSuchFieldException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUncheckedException("Could not access field "
					+ fieldName + " from object " + obj, e);
		} finally {
			if (f != null) {
				f.setAccessible(false);
			}
		}
	}

	/**
	 * Retrieve classes that belong to the specified package from directories and jars on the classpath. Does not include
	 * subpackages - use {@link #getClassesForPackage(String, boolean)} for this
	 * behaviour. note that this will not support zip files.
	 *
	 * @author dstaines (fixed behaviour for subclasses)
	 * @author Thanos.Panousis from
	 *         http://forums.sun.com/thread.jspa?messageID=10115467#10115467
	 * @param pckgname
	 *            name of package
	 * @return list of matching classes
	 * @throws ClassNotFoundException
	 */
	public static List<Class<?>> getClassesForPackage(String pckgname)
			throws ClassNotFoundException {
		return getClassesForPackage(pckgname, false);
	}

	/**
	 * Retrieve classes that belong to the specified package. note that this will not support zip files.
	 *
	 * @author dstaines (fixed behaviour for subclasses)
	 * @author Thanos.Panousis from
	 *         http://forums.sun.com/thread.jspa?messageID=10115467#10115467
	 * @param pckgname
	 *            name of package
	 * @param sub
	 *            if true, include subpackages
	 * @return list of matching classes
	 * @throws ClassNotFoundException
	 */
	public static List<Class<?>> getClassesForPackage(String pckgname,
			boolean sub) throws ClassNotFoundException {
		// This will hold a list of directories matching the pckgname.
		// There may be more than one if a package is split over multiple
		// jars/paths
		List<Class<?>> classes = new ArrayList<Class<?>>();
		List<File> directories = new ArrayList<File>();
		Pattern classPattern = null;
		if (sub) {
			classPattern = Pattern.compile("^" + pckgname.replace('.', '/')
					+ "[/A-z0-9_]+\\.class");
		} else {
			classPattern = Pattern.compile("^" + pckgname.replace('.', '/')
					+ "/[A-z0-9_]+\\.class");
		}
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(pckgname.replace('.',
					'/'));
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					JarURLConnection conn = (JarURLConnection) res
							.openConnection();
					JarFile jar = conn.getJarFile();
					for (JarEntry e : Collections.list(jar.entries())) {
						if (classPattern.matcher(e.getName()).matches()) {
							String className = e.getName().replace("/", ".")
									.substring(0, e.getName().length() - 6);
							classes.add(Class.forName(className));
						}
					}
				} else
					directories.add(new File(URLDecoder.decode(res.getPath(),
							"UTF-8")));
			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be "
					+ "a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be "
					+ "a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException(
					"IOException was thrown when trying "
							+ "to get all resources for " + pckgname);
		}

		// For every directory identified capture all the .class files
		for (File directory : directories) {
			classes.addAll(getClassesForDirectory(directory, pckgname, sub));
		}
		return classes;
	}

	protected static List<Class<?>> getClassesForDirectory(File directory,
			String pckgname, boolean sub) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (directory.exists() && directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.isDirectory() && sub) {
					classes.addAll(getClassesForDirectory(f, pckgname + "."
							+ f.getName(), sub));
				} else {
					String name = f.getName();
					if (name.matches("^[A-z0-9-]+\\.class$")) {
						// removes the .class extension
						try {
							String clazzName = pckgname + '.'
									+ name.substring(0, name.length() - 6);
							classes.add(Class.forName(clazzName));
						} catch (ClassNotFoundException e) {
							throw new ClassNotFoundException(pckgname + " ("
									+ f.getPath()
									+ ") does not appear to be a valid package");
						}
					}
				}
			}
		}
		return classes;
	}

}
