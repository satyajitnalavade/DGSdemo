package com.example.dgsdemo.scalars;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsRuntimeWiring;
import graphql.scalars.ExtendedScalars;
import graphql.schema.idl.RuntimeWiring;


/**
 * @author satya
 */
@DgsComponent
public class DateTimeScalar {

        @DgsRuntimeWiring
        public RuntimeWiring.Builder addScalar(RuntimeWiring.Builder builder) {
            return builder.scalar(ExtendedScalars.DateTime);
        }
}

