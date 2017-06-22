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
import org.terasology.events.PriceDeterminerEvent;
import org.terasology.events.TradeInitiatedEvent;
import org.terasology.factions.FactionComponent;
import org.terasology.factions.FactionSystem;
import org.terasology.registry.In;
import org.terasology.tradeFactionIntegration.policies.ExternalTradePolicy;
import org.terasology.tradeFactionIntegration.policies.InternalTradePolicy;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TradeFactionIntegrationSystem extends BaseComponentSystem {
    @In
    FactionSystem factionSystem;

    @Override
    public void postBegin() {
        ExternalTradePolicy externalTradePolicy = new ExternalTradePolicy();
        externalTradePolicy.tradeAllowed = false;
        factionSystem.saveTwoWayPolicy("Dwarves", "Elves", externalTradePolicy);
        factionSystem.saveInternalPolicy("Elves", new InternalTradePolicy());
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onTradeInitiatedEvent(TradeInitiatedEvent event, EntityRef entity, FactionComponent buyerFaction) {
        if(!event.seller.hasComponent(FactionComponent.class)) {
            return;
        }

        FactionComponent sellerFaction = event.seller.getComponent(FactionComponent.class);

        if(buyerFaction.name.equals(sellerFaction.name)) {
            return;
        }

        ExternalTradePolicy externalTradePolicy = factionSystem.getTwoWayPolicy(sellerFaction.name, buyerFaction.name,
                ExternalTradePolicy.class);

        if(externalTradePolicy == null) {
            return;
        }

        if(!externalTradePolicy.tradeAllowed) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void onPriceDeterminer(PriceDeterminerEvent event, EntityRef entity, FactionComponent sellerFaction) {
        if(!event.buyer.hasComponent(FactionComponent.class)) {
            return;
        }

        FactionComponent buyerFaction = event.buyer.getComponent(FactionComponent.class);

        if(buyerFaction.name.equals(sellerFaction.name)) {
            InternalTradePolicy internalTradePolicy = factionSystem.getInternalPolicy(buyerFaction.name
                    , InternalTradePolicy.class);

            if(internalTradePolicy == null) {
                return;
            }

            event.setPrice((int)(event.getPrice() * internalTradePolicy.discount));
        }
    }
}
