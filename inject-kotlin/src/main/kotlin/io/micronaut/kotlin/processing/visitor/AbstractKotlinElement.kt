package io.micronaut.kotlin.processing.visitor;

import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import io.micronaut.core.annotation.AnnotationMetadata
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.core.annotation.AnnotationValueBuilder
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.ArgumentUtils
import io.micronaut.inject.ast.Element
import java.util.function.Consumer
import java.util.function.Predicate

abstract class AbstractKotlinElement(private val declaration: KSNode,
                                     private var annotationMetadata: AnnotationMetadata,
                                     private val visitorContext: KotlinVisitorContext) : Element {

    override fun getNativeType(): KSNode {
        return declaration
    }

    override fun isProtected(): Boolean {
        return if (declaration is KSModifierListOwner) {
            declaration.modifiers.contains(Modifier.PROTECTED)
        } else {
            false
        }
    }

    override fun isPublic(): Boolean {
        return if (declaration is KSModifierListOwner) {
            declaration.modifiers.contains(Modifier.PUBLIC)
        } else {
            false
        }
    }

    override fun isPrivate(): Boolean {
        return if (declaration is KSModifierListOwner) {
            declaration.modifiers.contains(Modifier.PRIVATE)
        } else {
            false
        }
    }

    override fun getAnnotationMetadata(): AnnotationMetadata {
        return annotationMetadata
    }

    @NonNull
    override fun <T : Annotation?> annotate(
        @NonNull annotationType: String,
        @NonNull consumer: Consumer<AnnotationValueBuilder<T>?>
    ): Element? {
        ArgumentUtils.requireNonNull("annotationType", annotationType)
        ArgumentUtils.requireNonNull("consumer", consumer)
        val builder: AnnotationValueBuilder<T> = AnnotationValue.builder(annotationType)
        consumer.accept(builder)
        val av = builder.build()
        val annotationUtils = visitorContext.getAnnotationUtils()
        this.annotationMetadata = annotationUtils
            .newAnnotationBuilder()
            .annotate(annotationMetadata, av)
        updateMetadataCaches()
        return this
    }

    override fun <T : Annotation?> annotate(annotationValue: AnnotationValue<T>?): Element? {
        ArgumentUtils.requireNonNull("annotationValue", annotationValue)
        val annotationUtils = visitorContext.getAnnotationUtils()
        this.annotationMetadata = annotationUtils
            .newAnnotationBuilder()
            .annotate(annotationMetadata, annotationValue)
        updateMetadataCaches()
        return this
    }

    override fun removeAnnotation(@NonNull annotationType: String): Element? {
        ArgumentUtils.requireNonNull("annotationType", annotationType)
        return try {
            val annotationUtils = visitorContext.getAnnotationUtils()
            this.annotationMetadata = annotationUtils
                .newAnnotationBuilder()
                .removeAnnotation(annotationMetadata, annotationType)
            this
        } finally {
            updateMetadataCaches()
        }
    }

    override fun <T : Annotation?> removeAnnotationIf(@NonNull predicate: Predicate<AnnotationValue<T>?>?): Element? {
        if (predicate != null) {
            val annotationUtils = visitorContext.getAnnotationUtils()
            this.annotationMetadata = annotationUtils
                .newAnnotationBuilder()
                .removeAnnotationIf(annotationMetadata, predicate)
            return this
        }
        return this
    }

    override fun removeStereotype(@NonNull annotationType: String): Element? {
        ArgumentUtils.requireNonNull("annotationType", annotationType)
        return try {
            val annotationUtils = visitorContext.getAnnotationUtils()
            this.annotationMetadata = annotationUtils
                .newAnnotationBuilder()
                .removeStereotype(annotationMetadata, annotationType)
            this
        } finally {
            updateMetadataCaches()
        }
    }

    private fun updateMetadataCaches() {
        TODO("implement me")
    }
}
