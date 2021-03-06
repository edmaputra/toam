package io.github.edmaputra.toam.common.error

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import io.github.edmaputra.toam.common.VALIDATION_ERRORS
import io.github.edmaputra.toam.core.error.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class CommonExceptionResolver : DataFetcherExceptionResolverAdapter() {

    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        if (ex is ConstraintViolationException) {
            val messages = ex.constraintViolations.stream()
                .map { ValidationError(it.propertyPath.last().name, it.message) }
                .collect(Collectors.toList())

            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.ValidationError)
                .message(ex.message)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .extensions(mapOf(VALIDATION_ERRORS to messages))
                .build()
        } else if (ex is EntityNotFoundException) {
          return GraphqlErrorBuilder.newError()
            .errorType(ErrorType.DataFetchingException)
            .message(ex.message)
            .path(env.executionStepInfo.path)
            .location(env.field.sourceLocation)
            .build()
        }
        return super.resolveToSingleError(ex, env)
    }

}
