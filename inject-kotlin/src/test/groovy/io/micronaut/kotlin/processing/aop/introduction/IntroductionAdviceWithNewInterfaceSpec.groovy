package io.micronaut.kotlin.processing.aop.introduction

import io.micronaut.context.BeanContext
import io.micronaut.context.DefaultBeanContext
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.BeanFactory
import io.micronaut.inject.writer.BeanDefinitionVisitor
import spock.lang.Specification
import static io.micronaut.kotlin.processing.KotlinCompiler.*

class IntroductionAdviceWithNewInterfaceSpec extends Specification {

    void "test that it is possible for @Introduction advice to implement additional interfaces on concrete classes"() {
        when:
        BeanDefinition beanDefinition = buildBeanDefinition('test.MyBean' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test

import io.micronaut.kotlin.processing.aop.introduction.*
import io.micronaut.context.annotation.*

@ListenerAdvice
@Stub
@jakarta.inject.Singleton
open class MyBean  {

    @Executable
    fun getFoo() = "good"
}

''')
        then:
        !beanDefinition.isAbstract()
        beanDefinition != null
        ApplicationEventListener.class.isAssignableFrom(beanDefinition.beanType)
        beanDefinition.injectedFields.size() == 0
        beanDefinition.executableMethods.size() == 2
        beanDefinition.findMethod("getFoo").isPresent()
        beanDefinition.findMethod("onApplicationEvent", Object).isPresent()

        when:
        def context = new DefaultBeanContext()
        context.start()
        def instance = ((BeanFactory)beanDefinition).build(context, beanDefinition)
        ListenerAdviceInterceptor listenerAdviceInterceptor= context.getBean(ListenerAdviceInterceptor)
        listenerAdviceInterceptor.recievedMessages.clear()
        then:"the methods are invocable"
        listenerAdviceInterceptor.recievedMessages.isEmpty()
        instance.getFoo() == "good"
        instance.onApplicationEvent(new Object()) == null
        !listenerAdviceInterceptor.recievedMessages.isEmpty()

    }

    void "test that it is possible for @Introduction advice to implement additional interfaces on abstract classes"() {
        when:
        BeanDefinition beanDefinition = buildBeanDefinition('test.MyBean' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test

import io.micronaut.kotlin.processing.aop.introduction.*
import io.micronaut.context.annotation.*

@ListenerAdvice
@Stub
@jakarta.inject.Singleton
abstract class MyBean  {

    @Executable
    fun getFoo() = "good"
}

''')
        then:
        !beanDefinition.isAbstract()
        beanDefinition != null
        ApplicationEventListener.class.isAssignableFrom(beanDefinition.beanType)
        beanDefinition.injectedFields.size() == 0
        beanDefinition.executableMethods.size() == 2
        beanDefinition.findMethod("getFoo").isPresent()
        beanDefinition.findMethod("onApplicationEvent", Object).isPresent()

        when:
        def context = new DefaultBeanContext()
        context.start()
        def instance = ((BeanFactory)beanDefinition).build(context, beanDefinition)
        ListenerAdviceInterceptor listenerAdviceInterceptor= context.getBean(ListenerAdviceInterceptor)
        listenerAdviceInterceptor.recievedMessages.clear()
        then:"the methods are invocable"
        listenerAdviceInterceptor.recievedMessages.isEmpty()
        instance.getFoo() == "good"
        instance.onApplicationEvent(new Object()) == null
        !listenerAdviceInterceptor.recievedMessages.isEmpty()
    }

    void "test that it is possible for @Introduction advice to implement additional interfaces on interfaces"() {
        when:
        BeanDefinition beanDefinition = buildBeanDefinition('test.MyBean' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test

import io.micronaut.kotlin.processing.aop.introduction.*
import io.micronaut.context.annotation.*

@ListenerAdvice
@Stub
@jakarta.inject.Singleton
interface MyBean  {

    @Executable
    fun getBar(): String 
    
    @Executable
    fun getFoo() = "good"
}

''')
        then:
        !beanDefinition.isAbstract()
        beanDefinition != null
        ApplicationEventListener.class.isAssignableFrom(beanDefinition.beanType)
        beanDefinition.injectedFields.size() == 0
        beanDefinition.executableMethods.size() == 3
        beanDefinition.findMethod("getBar").isPresent()
        beanDefinition.findMethod("onApplicationEvent", Object).isPresent()

        when:
        def context = BeanContext.run()
        def instance = ((BeanFactory)beanDefinition).build(context, beanDefinition)
        ListenerAdviceInterceptor listenerAdviceInterceptor= context.getBean(ListenerAdviceInterceptor)
        listenerAdviceInterceptor.recievedMessages.clear()

        then:"the methods are invocable"
        listenerAdviceInterceptor.recievedMessages.isEmpty()
        instance.getFoo() == "good"
        instance.getBar() == null
        instance.onApplicationEvent(new Object()) == null
        !listenerAdviceInterceptor.recievedMessages.isEmpty()
        listenerAdviceInterceptor.recievedMessages.size() == 1

        cleanup:
        context.close()
    }

    void "test an interface with non overriding but subclass return type method"() {
        when:
        BeanDefinition beanDefinition = buildBeanDefinition('test.MyBean' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test

import io.micronaut.kotlin.processing.aop.introduction.*
import io.micronaut.context.annotation.*

@Stub
@jakarta.inject.Singleton
interface MyBean: GenericInterface, SpecificInterface

open class Generic

class Specific: Generic()

interface GenericInterface {   
    fun getObject(): Generic
}

interface SpecificInterface {    
    fun getObject(): Specific
}
''')

        then:
        noExceptionThrown()
        beanDefinition != null

        when:
        def context = new DefaultBeanContext()
        context.start()
        def instance = ((BeanFactory)beanDefinition).build(context, beanDefinition)

        then:
        //I ended up going this route because actually calling the methods here would be relying on
        //having the target interface in the bytecode of the test
        instance.$proxyMethods.length == 2
    }
}
