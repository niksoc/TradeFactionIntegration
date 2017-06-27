/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.tradeFactionIntegration.policies;

import org.terasology.factions.policies.FieldDescriptor;
import org.terasology.factions.policies.policies.TwoWayPolicy;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.List;

@MappedContainer
public class ExternalTradePolicy extends TwoWayPolicy {
    public boolean tradeAllowed = true;

    @Override
    public List<FieldDescriptor> getFieldDescriptions() {
        List<FieldDescriptor> fields = new ArrayList<>();
        fields.add(new FieldDescriptor("trade allowed",
                "" + tradeAllowed,
                "is trade allowed between the two factions"
                ));
        return fields;
    }
}
