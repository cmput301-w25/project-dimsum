package com.baobook.baobook.controller;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.CountDownLatch;

public class FirestoreTestUtils {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void clearFirestoreCollection(String collection) throws RuntimeException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        db.collection(collection).get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.getDocuments().forEach(doc -> doc.getReference().delete());
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to clear Firestore emulator.");
                });

        latch.await();
    }
}
