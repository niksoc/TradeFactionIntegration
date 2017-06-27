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
package org.terasology.tradeFactionIntegration;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.trade.events.PriceDeterminerEvent;
import org.terasology.trade.events.TradeInitiatedEvent;
import org.terasology.factions.FactionSystem;
import org.terasology.factions.components.FactionMemberComponent;
import org.terasology.registry.In;
import org.terasology.tradeFactionIntegration.policies.ExternalTradePolicy;
import org.terasology.tradeFactionIntegration.policies.InternalTradePolicy;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TradeFactionIntegrationSystem extends BaseComponentSystem {
    @In
    FactionSystem factionSystem;

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onTradeInitiatedEvent(TradeInitiatedEvent event, EntityRef entity, FactionMemberComponent buyerFaction) {
        if (!event.seller.hasComponent(FactionMemberComponent.class)) {
            return;
        }

        FactionMemberComponent sellerFaction = event.seller.getComponent(FactionMemberComponent.class);

        if (buyerFaction.name.equals(sellerFaction.name)) {
            return;
        }

        ExternalTradePolicy externalTradePolicy = factionSystem.getTwoWayPolicy(ExternalTradePolicy.class, sellerFaction.name, buyerFaction.name
        );

        if (externalTradePolicy == null) {
            return;
        }

        if (!externalTradePolicy.tradeAllowed) {
            event.consume();
        }

    }

    @ReceiveEvent
    public void onPriceDeterminer(PriceDeterminerEvent event, EntityRef entity, FactionMemberComponent sellerFaction) {
        if (!event.buyer.hasComponent(FactionMemberComponent.class)) {
            return;
        }

        FactionMemberComponent buyerFaction = event.buyer.getComponent(FactionMemberComponent.class);

        if (buyerFaction.name.equals(sellerFaction.name)) {
            InternalTradePolicy internalTradePolicy = factionSystem.getInternalPolicy(InternalTradePolicy.class, buyerFaction.name
            );

            if (internalTradePolicy == null) {
                return;
            }
            event.setPrice((int) (event.getBasePrice() * (1 - internalTradePolicy.discount)));
            event.addDeterminant("-" + internalTradePolicy.discount * 100 + "% : same faction discount");

        }
    }
}
