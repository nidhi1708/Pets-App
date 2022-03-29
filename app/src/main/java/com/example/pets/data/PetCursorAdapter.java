package com.example.pets.data;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.pets.R;

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context , Cursor c){ //Created a constructor
        super(context , c , 0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item , viewGroup , false); //Here we are inflating the list_item.xml file and passing to bindView
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tv_pet_name =(TextView) view.findViewById(R.id.textView); //finding the textViews
        TextView tv_pet_breed =(TextView) view.findViewById(R.id.textView2);

        String pet_name = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME));//Finding the pet name & breed through cursor
        String pet_breed=cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED));

        //unknown breed is not saved in database
        if(TextUtils.isEmpty(pet_breed)){
            pet_breed=context.getString(R.string.unknown_breed);
        }

        tv_pet_name.setText(pet_name);//finally set the name and breed of our pet
        tv_pet_breed.setText(pet_breed);

    }
}
//We dont need to move our cursor to next b/c it is already done in our cursorAdapter class which we are extending in PetCursorAdapter