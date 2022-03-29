package com.example.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

//we used final here because it is fixed
public final class PetContract {

    public static final String CONTENT_AUTHORITY="com.example.android.pets";
    public static final Uri BASE_CONTENT_URI=Uri.parse("content://"+CONTENT_AUTHORITY);
    // content://com.example.android.pets
    public static final String PATH_PETS="pets";

    private PetContract(){} //to avoid making instances of this class

    public static final class PetEntry implements BaseColumns{

        // content://com.example.android.pets/pets
        public static final Uri CONTENT_URI=Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PETS);//providing accessing to pets database


        //For getType() method in PetProvider
        //vnd.android.cursor.dir/com.example.android.pets/pets
        public static final String CONTENT_LIST_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PETS;

        //vnd.android.cursor.item/com.example.android.pets/pets
        public static final String CONTENT_ITEM_TYPE= ContentResolver.ANY_CURSOR_ITEM_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PETS;

        public static final String TABLE_NAME="pets";
        public static final String _ID=BaseColumns._ID;
        public static final String COLUMN_PET_NAME="name";
        public static final String COLUMN_PET_BREED="breed";
        public static final String COLUMN_PET_GENDER="gender";
        public static final String COLUMN_PET_WEIGHT="weight";

        public static final int GENDER_UNKNOWN=0;
        public static final int GENDER_MALE=1;
        public static final int GENDER_FEMALE=2;

        public static boolean isValidGender(int gender){
            if(gender==GENDER_UNKNOWN || gender==GENDER_MALE|| gender==GENDER_FEMALE ){
                return true;
            }
            return false;
        }

    }
}
