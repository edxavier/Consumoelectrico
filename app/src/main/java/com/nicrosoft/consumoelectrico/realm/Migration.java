package com.nicrosoft.consumoelectrico.realm;

import android.support.annotation.NonNull;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Eder Xavier Rojas on 23/11/2017.
 */

public class Migration implements RealmMigration {
    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            schema.get("Medidor")
                    .addField("name", String.class);
            oldVersion++;
        }
    }
}
