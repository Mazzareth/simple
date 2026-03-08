package app.masterwork.simple.stats.xp;

import java.util.Arrays;

import net.minecraft.resources.Identifier;

record XpRewardSource(XpRewardActivity activity, Identifier resourceId, Identifier professionId) {
    static XpRewardSource fromFileId(XpRewardActivity activity, Identifier resourceId) {
        String[] parts = resourceId.getPath().split("/");

        if (parts.length < 3) {
            throw new IllegalStateException("Invalid xp reward path '" + resourceId + "' for activity " + activity.directory()
                    + ". Expected <profession_namespace>/<profession_path>/<file>.");
        }

        String professionNamespace = parts[0];
        String professionPath = String.join("/", Arrays.copyOfRange(parts, 1, parts.length - 1));

        if (professionPath.isEmpty()) {
            throw new IllegalStateException("Invalid xp reward path '" + resourceId + "': missing profession path.");
        }

        return new XpRewardSource(activity, resourceId, Identifier.fromNamespaceAndPath(professionNamespace, professionPath));
    }

    String entrySource(int entryIndex) {
        return resourceId + "#entries[" + entryIndex + "]";
    }
}
