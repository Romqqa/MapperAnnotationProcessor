package com.ivanovrb.mappercompiler.factory

import javax.annotation.processing.ProcessingEnvironment


class ExtractorConstructorFieldsFromClassInPackage(
        graphDependencies: Map<String, String>,
        processingEnv: ProcessingEnvironment
) : ExtractorConstructorFields(graphDependencies, processingEnv) {

    override val primaryConstructor: Constructor by lazy {
            val constructors = getConstructorsFromElement(primaryElement)

             if (constructors.size == 1)
                constructors.first()
            else
                resolveConstructorsConflict(constructors)
        }
}



