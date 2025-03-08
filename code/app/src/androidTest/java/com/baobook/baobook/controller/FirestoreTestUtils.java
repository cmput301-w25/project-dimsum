package com.baobook.baobook.controller;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FirestoreTestUtils {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void clearFirestoreCollection(String collection) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(collection).get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();
                    querySnapshot.getDocuments().forEach(doc -> batch.delete(doc.getReference()));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> future.complete(null))
                            .addOnFailureListener(future::completeExceptionally);
                })
                .addOnFailureListener(future::completeExceptionally);

        try {
            future.get(10, TimeUnit.SECONDS); // Ensures Firestore clears before continuing
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Failed to clear Firestore collection: " + collection, e);
        }
    }
}
