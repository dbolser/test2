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

import java.lang.reflect.Method;
import java.text.MessageFormat;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;

/**
 * Duck typing is the idea that if it looks, walks and sounds like a duck
 * then it must be. Languages like Ruby (and Perl) have this since their data
 * structures respond to messages rather than the Java style of strongly
 * enforced associations. Duck typing means that you no longer care if
 * an object implements a specific inheritence structure; mearly that when
 * you run something on it, it's going to respond.
 *
 * <p>
 * It should be noted that this is not pure duck typing. In duck typing
 * you should be able to ask an object will it respond to a method and then
 * invoke said method. Since reflection requires access to the method instances
 * it really is not possible to apply this same procedure to this implementation
 * or to Java as a whole. Having to use interfaces can be seen as defining
 * a contract to duck type against. Plus there is nothing which says you
 * cannot have a class level interface and use that to apply contracts
 * against.
 *
 * <p>
 * This duck typing module tries to decouple this direct association by
 * hiding it behind an interface. The duck type proxy is created wrt an
 * interface which defines the methods you want to access. When you run a
 * method from the proxy object the proxy queries the backing object
 * for a method of the same signiture. If one can be found then it will be
 * invoked and its result returned.
 *
 * <p>
 * If a method cannot be found then a {@link ReflectionUncheckedException} is
 * thrown indicating an invalid request.
 *
 * <p>
 * If you do not want an exception thrown then I recommend using this object
 * as the basis for interacting with duck typed objects. Then create a
 * new intercept method for whatever is your new response. For
 * an example of using the DuckType in another interceptor instance please
 * look at {@link DelegatingProxy}
 *
 * <p>
 * As a side note on speed, this duck type method increases the cost of
 * a method call approx. x1.5 when running 2,000,000 method calls against
 * unproxied calls to the same backing object instance.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DuckType implements MethodInterceptor {

  /**
   * The main method to use when creating a duck typed object. This method
   * will create a CGLLIB {@link Enhancer} which can be seen as an equivalent
   * to a JDK {@link Proxy}. To create a proxy object run:
   *
   * <pre>
   * AnObject obj = new AnObject();
   * List proxiedList = DuckType.createProxy(List.class, obj);
   * proxiedList.add("Hello");
   * </pre>
   *
   * This example assumes that the object AnObject has a method called add
   * which can take an Object in. In this particular case it could be slightly
   * confusing that obj can respond to partial implementations (very useful
   * for mock object generation). If you do not want this situation then use
   * the {@link #createStrictProxy(Class, Object)} method
   *
   * @param <T> The intended type of Object to proxy over
   * @param intrface The interface to base the proxy on
   * @param obj The object which is the target "duck"
   * @return The proxy which will attempt to delegate to the given object
   * but there is no guarentee that it will actually run a method
   */
  public static <T> T createProxy(Class<T> intrface, Object obj) {
    return ProxyUtils.createCglibProxy(intrface, new DuckType(obj));
  }

  /**
   * This method will only work if the given object implements all methods
   * found in the given interface. If this is too strict then use
   * {@link #createProxy(Class, Object)}
   *
   * @param <T> The intended type of Object to proxy over
   * @param intrface The interface to base the proxy on
   * @param obj The object which is the target "duck"
   * @return The proxy which will delegate all interface methods to the object
   */
  public static <T> T createStrictProxy(Class<T> intrface, Object obj) {
    DuckType duck = new DuckType(obj);

    for(Method method: intrface.getMethods()) {
      if(!duck.hasMethod(method)) {
        String msg = "The given object {0} did not have the method {1} " +
            "and is not a strict implementation of the interface {2}";
        Object params = new Object[]{
            obj.getClass().getName(),
            method.toString(),
            intrface.getName()
        };
        throw new ReflectionUncheckedException(MessageFormat.format(msg, params));
      }
    }

    return ProxyUtils.createCglibProxy(intrface, duck);
  }

  private final Object duckObj;
  private FastClass fastClass = null;

  protected DuckType(Object duckObj) {
    if(duckObj == null) {
      throw new ReflectionUncheckedException("Input object cannot be null");
    }
    this.duckObj = duckObj;
    Class<?> targetClass = duckObj.getClass();
    fastClass = ReflectionUtils.getFastClass(targetClass);
  }

  protected Object getDuckObj() {
    return duckObj;
  }

  /**
   * For the given method this will query via {@link #getFastClass()} to see
   * if the underlying duck typed object has it
   */
  protected boolean hasMethod(Method method) {
    int index = getFastMethodIndex(method);
    return (index != ReflectionUtils.INVALID_METHOD_INDEX);
  }

  /**
   * Returns the index of the method as found in the duck typed object
   */
  protected int getFastMethodIndex(Method method) {
    return ReflectionUtils.getFastMethodIndex(fastClass, method);
  }

  /**
   * Wrapper around {@link FastClass#invoke(int, Object, Object[])} which will
   * invoke a specific index in an object with the given arguments. This is
   * done just as a convenience since it calls out to the local fast class and
   * the duck object
   */
  protected Object invoke(int index, Object[] args) throws Throwable {
    return fastClass.invoke(index, duckObj, args);
  }

  public Object intercept(Object obj, Method method, Object[] args,
      MethodProxy proxy) throws Throwable {

    int index = getFastMethodIndex(method);
    //Follows same rules as assertion but here because of speed
    if(index != ReflectionUtils.INVALID_METHOD_INDEX) {
      return invoke(index, args);
    }
    else {
      String msg = MessageFormat.format(
          "Could not find method {0} for class {1}",
          method.getName(),
          obj.getClass().getName()
      );
      throw new ReflectionUncheckedException(msg);
    }
  }
}
