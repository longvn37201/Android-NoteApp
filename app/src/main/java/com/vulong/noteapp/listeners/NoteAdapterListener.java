package com.vulong.noteapp.listeners;

import com.vulong.noteapp.entities.Note;

public interface NoteAdapterListener {
    void onNoteSelected(int pos, Note note);
}
