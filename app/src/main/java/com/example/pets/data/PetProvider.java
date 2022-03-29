package com.example.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.pets.data.PetContract.PetEntry;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {
    private  PetDbHelper mDbHelper;

    private static final int PETS=100;
    private static final int PETS_ID=101;

    private static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    //static runs first when we call any method from PetProvider
    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY , PetContract.PATH_PETS , PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY , PetContract.PATH_PETS+"/#" , PETS_ID);
    }
    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper=new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db=mDbHelper.getReadableDatabase(); // for reading our pet database

        //Initialising a cursor with null values
        Cursor cursor =db.query(PetEntry.TABLE_NAME , null , null , null , null , null , null);// creates the bundle of required rows according to our needs

        int match=sUriMatcher.match(uri); //matches the given uri with the defined uri's

        switch (match){
            case PETS: // we will get all the rows from pets table
                cursor= db.query(PetEntry.TABLE_NAME , projection ,selection , selectionArgs , null , null , sortOrder);
                break;

            case PETS_ID: // we will get seleted rows from the pets table
                selection=PetEntry._ID + "=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))}; // extracting the id's from our given uri , the id is written after #
                cursor = db.query(PetEntry.TABLE_NAME , projection ,selection , selectionArgs , null , null , sortOrder);
                break;

            default: new IllegalAccessException("Cannot query unknown uri "+uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver() , uri); //setting notification if there is any change
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri , contentValues); //Calling the helper method insertPet
            default:
                new IllegalAccessException("Inserting is not supported with "+uri);

        }
        return  null;
    }

    private Uri insertPet(Uri uri , ContentValues contentValues){
        //Checking User has entered the pet name
        String name= contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        if(name==null){
            throw new IllegalArgumentException("Pet name is missing");
        }

        //Checking if the gender is valid
        Integer gender=contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if(gender==null || !PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet gender is missing");
        }

        //Checking if weight is null or less than 0kgs
        Integer weight=contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight==null && weight<0){
            throw new IllegalArgumentException("Enter a valid Pet Weight");
        }

        //No need to check the breed of the pet

        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        //inserting new row
        long newRowid = db.insert(PetEntry.TABLE_NAME , null , contentValues); //returns a row id which is inserted

        if(newRowid==-1){
            Log.e(LOG_TAG , "Inserion failed");
            return null;
        }

        getContext().getContentResolver().notifyChange(uri , null);//We have used notifyChange() because we want all the listeners to know about the change
                                                                           //but in query method we used setNotificationChange() b/c we don't need to tell all the listeners about the change
        //returning new uri with row id at which the new pet is inserted
        return ContentUris.withAppendedId(uri , newRowid);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri , contentValues , selection , selectionArgs);

            case PETS_ID:
                selection=PetEntry._ID+"=?"; //Updating row on the bases of id
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))}; //checking the id
                return updatePet(uri , contentValues , selection , selectionArgs);

            default:
                throw new IllegalArgumentException("Update fail on this uri"+uri);
        }

    }


    //Helper method for Update
    private int updatePet(Uri uri , ContentValues contentValues , String selection , String[] selectionArgs){
        if(contentValues.containsKey(PetEntry.COLUMN_PET_NAME)){
            String name= contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
            if(name==null){
                throw new IllegalArgumentException("Pet name is missing");
            }
        }

        if(contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)){
            //Checking if the gender is valid
            Integer gender=contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(gender==null || !PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet gender is missing");
            }
        }

        if(contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            //Checking if weight is null or less than 0kgs
            Integer weight=contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight==null && weight<0){
                throw new IllegalArgumentException("Enter a valid Pet Weight");
            }
        }

        if(contentValues.size()==0){
            return 0;
        }

        SQLiteDatabase database=mDbHelper.getWritableDatabase();
        int newRow =database.update(PetEntry.TABLE_NAME , contentValues , selection , selectionArgs);
        if(newRow!=0){
            getContext().getContentResolver().notifyChange(uri , null);
        }
        return newRow;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database=mDbHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match){
            case PETS:
                //Deleting is performed on all rows
                rowsDeleted= database.delete(PetEntry.TABLE_NAME , selection , selectionArgs);
                break;

            case PETS_ID:
                //Deleting is performed on specific id
                selection=PetEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted= database.delete(PetEntry.TABLE_NAME , selection , selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Delete is not performed on this uri "+uri);
        }
        if(rowsDeleted!=0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match=sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;

            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }
    }
}
