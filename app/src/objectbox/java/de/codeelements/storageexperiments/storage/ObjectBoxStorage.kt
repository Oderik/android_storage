package de.codeelements.storageexperiments.storage

import android.app.Application
import de.codeelements.storageexperiments.model.Note
import de.codeelements.storageexperiments.storage.objectbox.MyObjectBox
import de.codeelements.storageexperiments.storage.objectbox.ObjectBoxNote
import de.codeelements.storageexperiments.storage.objectbox.ObjectBoxNote_
import io.objectbox.rx.RxQuery
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ObjectBoxStorage(application: Application) : Storage {
    private val objectBox = MyObjectBox.builder().androidContext(application).build()

    private val noteBox = objectBox.boxFor(ObjectBoxNote::class.java)
    override val notesObservable: Observable<List<Note>>
        get() = RxQuery.observable(noteBox.query().build()).map {
            it.map { objectBoxNote: ObjectBoxNote ->
                Note(objectBoxNote.id, objectBoxNote.title, objectBoxNote.text)
            }
        }

    override fun observeNote(id: Long): Observable<Note> =
        RxQuery.observable(noteBox.query().equal(ObjectBoxNote_.id, id).build())
            .filter {
                it.isNotEmpty()
            }.map {
                val objectBoxNote = it[0]
                Note(objectBoxNote.id, objectBoxNote.title, objectBoxNote.text)
            }

    override fun store(note: Note): Completable =
        Completable.fromAction {
            noteBox.put(ObjectBoxNote(note.id, note.title, note.text))
        }.subscribeOn(Schedulers.io())

    override fun remove(note: Note): Completable =
        Completable.fromAction {
            noteBox.remove(note.id!!)
        }.subscribeOn(Schedulers.io())

}
