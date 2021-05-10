package com.vulong.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vulong.noteapp.R;
import com.vulong.noteapp.adapter.NoteAdapter;
import com.vulong.noteapp.database.NotesDatabase;
import com.vulong.noteapp.entities.Note;
import com.vulong.noteapp.listeners.NoteAdapterListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NoteAdapterListener {

    private static final int REQUEST_CODE_ADD_NOTE = 69;

    private static final int REQUEST_CODE_UPDATE_NOTE = 966;
    private static final int REQUEST_CODE_SHOW_ALL_NOTE = 1231246;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 918237;
    private static final int REQUEST_CODE_PICK_IMAGE = 4388;

    private int noteClickedPos = -1;
    private Note noteClicked;

    private static final String TAG = "test";


    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> noteArrayList;

    private ImageView imgClearText;
    private EditText edtSearchNote;
    private ImageView imgNewNote;
    private ImageView imgQuickAddLink;
    private ImageView imgQuickAddImage;
    private LinearLayout layoutQuickAction;
    private AlertDialog enterUrlDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();

        getNote(REQUEST_CODE_SHOW_ALL_NOTE);

        //new note click
        imgNewNote.setOnClickListener(v ->
                startActivityForResult(new Intent(this, NewNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE));


        //search note
        edtSearchNote.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                noteAdapter.getFilter().filter(s.toString().trim());
                if (!s.toString().trim().isEmpty()) {
                    imgNewNote.setVisibility(View.GONE);
                    layoutQuickAction.setVisibility(View.GONE);
                    imgClearText.setVisibility(View.VISIBLE);
                } else {
                    imgNewNote.setVisibility(View.VISIBLE);
                    layoutQuickAction.setVisibility(View.VISIBLE);
                    imgClearText.setVisibility(View.INVISIBLE);
                }
            }

        });

        //clear text
        imgClearText.setOnClickListener(v -> {
            edtSearchNote.setText("");
            edtSearchNote.clearFocus();
            //hide keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

        });

        //quick add link
        imgQuickAddLink.setOnClickListener(v -> {
            performQuickAddUrlDialog();
        });

        //quick add img
        imgQuickAddImage.setOnClickListener(v -> {
            //check request permission
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                startActivityQuickSelectImageNote();
            } else {
                //request per
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                }
            }
        });

    }


    private void initFields() {
        imgQuickAddImage = findViewById(R.id.img_quick_add_image);
        imgQuickAddLink = findViewById(R.id.img_quick_add_link);
        imgClearText = findViewById(R.id.img_clear_text);
        layoutQuickAction = findViewById(R.id.layout_quick_action);
        imgNewNote = findViewById(R.id.img_new_note);
        edtSearchNote = findViewById(R.id.edt_search_note);
        noteArrayList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteArrayList, this);
        recyclerView = findViewById(R.id.recyclerview_note_list);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,
                        StaggeredGridLayoutManager.VERTICAL)
        );
        recyclerView.setAdapter(noteAdapter);


    }

    private void getNote(int reqCode) {


        @SuppressLint("StaticFieldLeak")
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getInstance(getApplicationContext()).noteDAO().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (reqCode == REQUEST_CODE_SHOW_ALL_NOTE) {
                    noteArrayList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();


                }
                if (reqCode == REQUEST_CODE_ADD_NOTE) {
                    noteArrayList.add(0, notes.get(0));

                    noteAdapter.notifyItemInserted(0);
                    noteAdapter.notifyItemRangeChanged(0, noteAdapter.getItemCount());

                    (findViewById(R.id.scrollView)).scrollTo(0, 0);

                    recyclerView.smoothScrollToPosition(0);

                }

                if (reqCode == REQUEST_CODE_UPDATE_NOTE) {
                    if (edtSearchNote.getText().toString().trim().isEmpty()) {
                        noteArrayList.remove(noteClickedPos);
                        noteArrayList.add(noteClickedPos, notes.get(noteClickedPos));
                        noteAdapter.notifyItemChanged(noteClickedPos);
                    } else {
                        noteArrayList = (ArrayList<Note>) notes;
                        noteAdapter.setNoteListSource((ArrayList<Note>) notes);
                        noteAdapter.getFilter().filter(edtSearchNote.getText().toString().trim());

                    }

                }

//                Log.d(TAG, "mainnn: " + noteArrayList.toString());
//                Log.d(TAG, "filter " + noteAdapter.getNoteListFiltered().toString());
//                Log.d(TAG, "source " + noteAdapter.getNoteListSource().toString());

            }
        }

        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNote(REQUEST_CODE_ADD_NOTE);
        }

        if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNote(REQUEST_CODE_UPDATE_NOTE);
            }
        }

        if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == NewNoteActivity.RESULT_DELETE) {
            if (edtSearchNote.getText().toString().trim().isEmpty()) {

                noteArrayList.remove(noteClickedPos);
//            noteAdapter.notifyDataSetChanged();
                noteAdapter.notifyItemRemoved(noteClickedPos);
                noteAdapter.notifyItemRangeChanged(noteClickedPos, noteAdapter.getItemCount());
            } else {

                noteArrayList.remove(noteClicked);
                noteAdapter.setNoteListSource(noteArrayList);
                noteAdapter.getFilter().filter(edtSearchNote.getText().toString().trim());

            }

//            Log.d(TAG, "mainnn: " + noteArrayList.toString());
//            Log.d(TAG, "filter " + noteAdapter.getNoteListFiltered().toString());
//            Log.d(TAG, "source " + noteAdapter.getNoteListSource().toString());

        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImgUri = data.getData();
            String imgPath= getPathFromURI(selectedImgUri);
            Intent intent = new Intent(this, NewNoteActivity.class);
            intent.putExtra("isQuickAddImg", true);
            intent.putExtra("imgPath", imgPath);
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
        }
    }

    @Override
    public void onNoteSelected(int pos, Note note) {
        noteClickedPos = pos;
        noteClicked = note;
        Intent intent = new Intent(this, NewNoteActivity.class);

        intent.putExtra("note", note);
        intent.putExtra("isUpdate", true);
//        Log.d(TAG, "onNoteSelected: "+noteClickedPos);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

    }

    private void performQuickAddUrlDialog() {
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
//                tvURL.setVisibility(View.VISIBLE);
//                tvURL.setText(inputURL.getText().toString().trim());
//                enterUrlDialog.dismiss();
//                imgDeleteUrl.setVisibility(View.VISIBLE);
                Intent intent = new Intent(this, NewNoteActivity.class);
                intent.putExtra("isQuickAddUrl", true);
                intent.putExtra("url", inputURL.getText().toString().trim());
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                enterUrlDialog.dismiss();
            }


        });

        view.findViewById(R.id.tv_cancel_link).setOnClickListener(v -> enterUrlDialog.dismiss());

        enterUrlDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityQuickSelectImageNote();
            } else {
                Toast.makeText(this, "VUI LÒNG CẤP QUYỀN TRUY CẬP BỘ NHỚ\nĐỂ THỰC HIỆN CHỨC NĂNG NÀY\n:((", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startActivityQuickSelectImageNote() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
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
}