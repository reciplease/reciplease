package org.reciplease;

import lombok.RequiredArgsConstructor;
import org.reciplease.service.InventoryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * One-time cleanup for {@link org.reciplease.model.InventoryItem}s left sitting at {@code
 * remaining <= 0} from before binning/consuming an item archived-and-deleted it instead. Safe to
 * leave running indefinitely: once every such item has been archived, nothing can reach zero
 * remaining and stay live again (see {@code InventoryService.saveOrArchive}), so this becomes a
 * no-op on every startup after the first.
 */
@Component
@RequiredArgsConstructor
public class ZeroRemainingInventoryBackfillRunner implements ApplicationRunner {

    private final InventoryService inventoryService;

    @Override
    public void run(final ApplicationArguments args) {
        inventoryService.archiveAllZeroRemainingItems();
    }
}
