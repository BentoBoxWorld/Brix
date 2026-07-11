package world.bentobox.brix;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class BrixPladdon extends Pladdon {
    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new Brix();
        }
        return addon;
    }
}
