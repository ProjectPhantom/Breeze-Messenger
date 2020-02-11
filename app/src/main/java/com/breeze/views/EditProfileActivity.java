package com.breeze.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import android.widget.TextView;

import android.os.Bundle;
import android.widget.Toast;
import com.breeze.R;
import com.breeze.application.BreezeAPI;
import com.breeze.datatypes.BrzNode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.breeze.R;
import com.breeze.application.BreezeAPI;
import com.breeze.datatypes.BrzNode;
import com.breeze.graph.BrzGraph;


public class EditProfileActivity extends Fragment
{
    private static final int READ_REQUEST_CODE = 42;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_edit_profile_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BreezeAPI api = BreezeAPI.getInstance();
        //Toast.makeText(getActivity(), api.hostNode.name, Toast.LENGTH_LONG).show();

        boolean editClicked = false;

        //set image
        ImageView profileImage = getView().findViewById(R.id.profile_image_edit);
        profileImage.setImageBitmap(api.storage.getProfileImage(api.storage.PROFILE_DIR, api.hostNode.id));

        //set username
        TextView userName = getView().findViewById(R.id.user_name);
        if(api.hostNode.name == null)
        {
            String s1 = "[no username entered]";
            userName.setTextColor(Color.RED);
            userName.setText(s1);
        }
        else
        {
            userName.setText(api.hostNode.name);
        }

        //set alias name
        TextView aliasName = getView().findViewById(R.id.alias_name);
        if(api.hostNode.id.isEmpty())
        {
            String s = "[no alias entered]";
            aliasName.setTextColor(Color.RED);
            aliasName.setText(s);
        }
        else
        {
            aliasName.setText(api.hostNode.alias);
        }
        //set save button
        Button saveButton = getView().findViewById(R.id.edit_save_button);
        saveButton.setVisibility (View.INVISIBLE);

        //edit picture dialog pop up and save
        Button editPicture = getView().findViewById(R.id.edit_image_button);

        editPicture.setOnClickListener(e -> {
            saveButton.setVisibility (View.VISIBLE);
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, READ_REQUEST_CODE);
        });

        //edit username dialog pop up and save
        Button editUserNameButton = getView().findViewById(R.id.edit_user_button);
        editUserNameButton.setOnClickListener(e ->{

            final EditText txtNewName = new EditText(getContext());
            txtNewName.setMaxWidth(10);

            new AlertDialog.Builder(getContext())
                    .setTitle("Edit Username")
                    .setMessage("Enter new username.")
                    .setView(txtNewName)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String newName = txtNewName.getText().toString();
                            updateNewUsername(newName);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();

        });

        //edit alias dialog pop up and save
        Button editAliasButton = getView().findViewById(R.id.edit_alias_button);
        editAliasButton.setOnClickListener(e ->{

            final EditText txtNewAlias = new EditText(getContext());
            txtNewAlias.setMaxWidth(10);

            new AlertDialog.Builder(getContext())
                    .setTitle("Edit Alias")
                    .setMessage("Enter new Alias.")
                    .setView(txtNewAlias)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String newAlias = txtNewAlias.getText().toString();
                            updateNewAlias(newAlias);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();

        });

        //using save button save changes to the state
    }
    public void updateNewUsername(String newUsername)
    {
        BreezeAPI.getInstance().state.getHostNode().name = newUsername;
    }

    public void updateNewAlias(String newAlias)
    {
        String newA = "";
        newA += "@" + newAlias;
        BreezeAPI.getInstance().state.getHostNode().alias = newA;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        ActionBar ab = activity.getSupportActionBar();
        if (ab == null) return;
        ab.setTitle("Edit Profile");


    }



}
