package com.ivanovrb.mappercompiler.factory

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements


class ExtractorConstructorFieldsFromClassInPackage(
        private val elementsUtils: Elements,
        graphDependencies: Map<String, String>,
        processingEnv: ProcessingEnvironment
) : ExtractorConstructorFields(elementsUtils, graphDependencies, processingEnv) {

    override val primaryConstructor: Constructor by lazy {
            val constructors = getConstructorsFromElement(primaryElement)

             if (constructors.size == 1)
                constructors.first()
            else
                resolveConstructorsConflict(constructors)
        }
}



