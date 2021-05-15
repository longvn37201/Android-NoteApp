package com.vulong.noteapp.activities;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.vulong.noteapp.R;
import com.vulong.noteapp.database.NotesDatabase;
import com.vulong.noteapp.entities.Note;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class NewNoteActivity extends AppCompatActivity {
    private static final String TAG = "test";
    public static final int RESULT_DELETE = 13128;

    private ImageView imgBack;
    private ImageView imgSave;
    private ImageView imgDeleteNote;

    private EditText edtTitle, edtContent;
    private TextView tvDatetime;
    private ImageView imgImageNote;
    private TextView tvURL;

    private ImageView imgDeleteUrl;
    private ImageView imgDeleteImage;

    private Note currentNote;

    // enter url dialog
    private AlertDialog enterUrlDialog;
    // delete note confirm dialog
    private AlertDialog confirmDeleteNote;

    //for save ROOM database
    private int selectColor;
    private String selectImage;

    private ImageView imgColor1;
    private ImageView imgColor2;
    private ImageView imgColor3;
    private ImageView imgColor4;
    private ImageView imgColor5;


    private static final int REQUEST_CODE_STORAGE_PERMISSION = 69;
    private static final int REQUEST_CODE_PICK_IMAGE = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);


        initFields();

        //is quick add url
        if (getIntent().getBooleanExtra("isQuickAddUrl", false)) {
            tvURL.setVisibility(View.VISIBLE);
            tvURL.setText(getIntent().getStringExtra("url").trim());
            imgDeleteUrl.setVisibility(View.VISIBLE);
        }

        //is quick add img
        if (getIntent().getBooleanExtra("isQuickAddImg", false)) {

            try {
                String imgPath=getIntent().getStringExtra("imgPath");

                File file = new File(imgPath);
                if (file.exists()) {
                    imgImageNote.setImageBitmap(BitmapFactory.decodeFile(imgPath));
                    selectImage = imgPath;
                    imgDeleteImage.setVisibility(View.VISIBLE);

                }
            } catch (Exception e) {
            }
        }

        //back
        imgBack.setOnClickListener(v -> onBackPressed());

        //save note
        imgSave.setOnClickListener(v -> saveNote());

        //delete url
        imgDeleteUrl.setOnClickListener(v -> {
            tvURL.setText(null);
            tvURL.setVisibility(View.GONE);
            imgDeleteUrl.setVisibility(View.GONE);
        });

        //delete img
        imgDeleteImage.setOnClickListener(v -> {
            selectImage = null;
            imgImageNote.setImageBitmap(null);
            imgImageNote.setVisibility(View.GONE);
            imgDeleteImage.setVisibility(View.GONE);
        });

        //delete note img
        if (currentNote != null) {
            imgDeleteNote.setVisibility(View.VISIBLE);
        }

        //delete note
        imgDeleteNote.setOnClickListener(v -> {
            performConfirmDeleteNoteialog();
        });


    }

    private void setNoteToUpdate() {
        edtTitle.setText(currentNote.getTitle());
        edtContent.setText(currentNote.getContent());
        tvDatetime.setText(getString(R.string.last_edit) + currentNote.getDateTime());
        try {
            File file = new File(currentNote.getImagePath());
            if (file.exists()) {
                imgImageNote.setImageBitmap(BitmapFactory.decodeFile(currentNote.getImagePath()));
                selectImage = currentNote.getImagePath();
                imgDeleteImage.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {
        }

        if (currentNote.getWebLink() != null && !currentNote.getWebLink().trim().isEmpty()) {
            tvURL.setText(currentNote.getWebLink().trim());
            tvURL.setVisibility(View.VISIBLE);
            imgDeleteUrl.setVisibility(View.VISIBLE);

        }

        selectColor = currentNote.getColor();
        CoordinatorLayout coordinatorLayout = findViewById(R.id.container_new_note);
        coordinatorLayout.setBackgroundColor(selectColor);
        synStatusBarWithColorNote();


    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter title!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();

        note.setTitle(title);
        note.setContent(content);
        note.setDateTime(
                new SimpleDateFormat("dd MMM, HH:mm a",
                        Locale.getDefault()).format(new Date())
        );
        note.setColor(selectColor);
        note.setImagePath(selectImage);

        if (tvURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(tvURL.getText().toString().trim());
        }

        if (currentNote != null) {
            note.setId(currentNote.getId());
        }


//        save note
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getInstance(getApplicationContext()).noteDAO().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();


    }

    private void initFields() {

        //default color
        selectColor = getResources().getColor(R.color.colorDefaultNoteColor);

        //default stt bar color
        synStatusBarWithColorNote();

        //default color pick when go new activity
        ((ImageView) findViewById(R.id.view_color1)).setImageResource(R.drawable.ic_done);

        //init fields
        imgSave = findViewById(R.id.img_done_note);
        imgBack = findViewById(R.id.img_back_home);
        imgDeleteNote = findViewById(R.id.img_delete_note);
        edtTitle = findViewById(R.id.edt_title);
        edtContent = findViewById(R.id.edt_content);
        tvDatetime = findViewById(R.id.tv_date_time_in_new_note);
        imgImageNote = findViewById(R.id.img_pic_in_new_note);
        imgDeleteImage = findViewById(R.id.img_remove_image);
        imgDeleteUrl = findViewById(R.id.img_remove_url);

        //set current time
        tvDatetime.setText(
                new SimpleDateFormat("dd MMM, HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        //init pick color fields
        imgColor1 = findViewById(R.id.view_color1);
        imgColor2 = findViewById(R.id.view_color2);
        imgColor3 = findViewById(R.id.view_color3);
        imgColor4 = findViewById(R.id.view_color4);
        imgColor5 = findViewById(R.id.view_color5);

        tvURL = findViewById(R.id.tv_web_url);

        //init for bottom sheet fields
        initBottomSheet();

        //init NOTE to update
        if (getIntent().getBooleanExtra("isUpdate", false)) {
            currentNote = (Note) getIntent().getSerializableExtra("note");
            setNoteToUpdate();
        }


    }


    //bottom sheet
    private void initBottomSheet() {

        //init pick color sheet
        final LinearLayout layoutBottomSheet = findViewById(R.id.layout_bottom_sheet);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        //when click on bottom sheet
        layoutBottomSheet.findViewById(R.id.tv_morefeature).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }


        });

        imgColor1.setOnClickListener(v -> {
            //set color for save
            selectColor = getResources().getColor(R.color.colorDefaultNoteColor);

            //set pick color icon
            imgColor1.setImageResource(R.drawable.ic_done);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);

            //set activity color
            CoordinatorLayout coordinatorLayout = findViewById(R.id.container_new_note);
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.colorDefaultNoteColor));

            //set stt bar color syn
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorDefaultNoteColor));
        });

        imgColor2.setOnClickListener(v -> {
            selectColor = getResources().getColor(R.color.colorNoteColor2);

            imgColor1.setImageResource(0);
            imgColor2.setImageResource(R.drawable.ic_done);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);

            CoordinatorLayout coordinatorLayout = findViewById(R.id.container_new_note);
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.colorNoteColor2));

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorNoteColor2));
        });

        imgColor3.setOnClickListener(v -> {
            selectColor = getResources().getColor(R.color.colorNoteColor3);

            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(R.drawable.ic_done);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            CoordinatorLayout coordinatorLayout = findViewById(R.id.container_new_note);
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.colorNoteColor3));

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorNoteColor3));
        });

        imgColor4.setOnClickListener(v -> {
            selectColor = getResources().getColor(R.color.colorNoteColor4);

            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(R.drawable.ic_done);
            imgColor5.setImageResource(0);
            CoordinatorLayout coordinatorLayout = findViewById(R.id.container_new_note);
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.colorNoteColor4));

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorNoteColor4));
        });

        imgColor5.setOnClickListener(v -> {
            selectColor = getResources().getColor(R.color.colorNoteColor5);

            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(R.drawable.ic_done);
            CoordinatorLayout coordinatorLayout = findViewById(R.id.container_new_note);
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.colorNoteColor5));

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorNoteColor5));
        });

        if (currentNote != null) {
            switch (currentNote.getColor()) {
                case R.color.colorDefaultNoteColor:
                    imgColor1.performClick();
                    break;
                case R.color.colorNoteColor2:
                    imgColor2.performClick();
                    break;
                case R.color.colorNoteColor3:
                    imgColor3.performClick();
                    break;
                case R.color.colorNoteColor4:
                    imgColor4.performClick();
                    break;
                case R.color.colorNoteColor5:
                    imgColor5.performClick();
                    break;
            }
        }

        //add picture
        layoutBottomSheet.findViewById(R.id.layout_add_image_in_new_note).setOnClickListener(v -> {

            //collapse bottom sheet
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


            //check request permission
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                startActivitySelectImageNote();
            } else {
                //request per
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                }
            }

        });


        //add link
        layoutBottomSheet.findViewById(R.id.layout_add_link_in_new_note).setOnClickListener(v -> {
            //collapse bottom sheet
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            performAddUrlDialog();


        });

    }

    private void startActivitySelectImageNote() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void synStatusBarWithColorNote() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(selectColor);
    }

    //result storage per
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivitySelectImageNote();
            } else {
                Toast.makeText(this,
                        getString(R.string.please_accept_storage_per),
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    //pick image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImgUri = data.getData();

            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImgUri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                imgImageNote.setImageBitmap(bitmap);

                //convert image bitmap to string
                selectImage = getPathFromURI(selectedImgUri);


                imgImageNote.setVisibility(View.VISIBLE);
                imgDeleteImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getPathFromURI(Uri selectedImgUri) {

        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImgUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return picturePath;
    }

    private void performAddUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.layout_add_url,
                null
        );
        builder.setView(view);

        enterUrlDialog = builder.create();

        if (enterUrlDialog.getWindow() != null) {
            enterUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        EditText inputURL = view.findViewById(R.id.edt_url_input);
        inputURL.requestFocus();

        view.findViewById(R.id.tv_add_link).setOnClickListener(v -> {
            if (inputURL.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            } else {
                tvURL.setVisibility(View.VISIBLE);
                tvURL.setText(inputURL.getText().toString().trim());
                enterUrlDialog.dismiss();
                imgDeleteUrl.setVisibility(View.VISIBLE);
            }


        });

        view.findViewById(R.id.tv_cancel_link).setOnClickListener(v -> enterUrlDialog.dismiss());

        enterUrlDialog.show();

    }

    private void performConfirmDeleteNoteialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note,
                null
        );
        builder.setView(view);

        confirmDeleteNote = builder.create();

        if (confirmDeleteNote.getWindow() != null) {
            confirmDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }


        view.findViewById(R.id.tv_confirm_delete).setOnClickListener(v -> {

            class DeleteNoteTask extends AsyncTask<Void, Void, Void> {
                @Override
                protected Void doInBackground(Void... voids) {
                    NotesDatabase.getInstance(getApplicationContext()).noteDAO().deleteNote(currentNote);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent intent = new Intent();
                    setResult(RESULT_DELETE, intent);
                    finish();
                }
            }
            new DeleteNoteTask().execute();
            confirmDeleteNote.dismiss();


        });

        view.findViewById(R.id.tv_cancel_delete).setOnClickListener(v -> confirmDeleteNote.dismiss());

        confirmDeleteNote.show();
    }

}