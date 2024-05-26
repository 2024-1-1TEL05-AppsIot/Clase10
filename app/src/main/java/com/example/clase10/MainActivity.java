package com.example.clase10;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clase10.databinding.ActivityMainBinding;
import com.example.clase10.dtos.Usuario;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    ActivityMainBinding binding;
    ListenerRegistration snapshotListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.button.setOnClickListener(view -> {
            String nombre = binding.textFieldNombre.getEditText().getText().toString();
            String apellido = binding.textFieldApellido.getEditText().getText().toString();
            String edadStr = binding.textFieldEdad.getEditText().getText().toString();
            String dni = binding.textFieldDni.getEditText().getText().toString();

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEdad(Integer.parseInt(edadStr));

            db.collection("usuarios")
                    .document(dni)
                    .set(usuario)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Usuario grabado", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Algo pasó al guardar ", Toast.LENGTH_LONG).show();
                    });
        });


        binding.btnListarUsuarios.setOnClickListener(view -> {
            String dni = binding.textFieldDni.getEditText().getText().toString();

            if (!dni.isEmpty()) {
                binding.btnListarUsuarios.setEnabled(false);
                db.collection("usuarios")
                        .document(dni)
                        .get()
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    Log.d("msg-test", "DocumentSnapshot data: " + documentSnapshot.getData());

                                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                    Toast.makeText(this, "Nombre: " + usuario.getNombre() + " | apellido: " + usuario.getApellido(), Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(this, "El usuario no existe", Toast.LENGTH_LONG).show();
                                }
                            }

                            binding.btnListarUsuarios.setEnabled(true);
                        });
            }

            /*binding.floatingActionButton.setOnClickListener(view2 -> {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            });*/

        });

        binding.btnTiempoReal.setOnClickListener(view -> {

            snapshotListener = db.collection("usuarios")
                    .orderBy("edad", Query.Direction.DESCENDING)
                    .limit(4)
                    .addSnapshotListener((collection, error) -> {

                        if (error != null) {
                            Log.w("msg-test", "Listen failed.", error);
                            return;
                        }

                        Log.d("msg-test", "---- Datos en tiempo real ----");
                        for (QueryDocumentSnapshot doc : collection) {
                            Usuario usuario = doc.toObject(Usuario.class);
                            Log.d("msg-test",
                                    "id: " + doc.getId() +
                                            "| Nombre: " + usuario.getNombre() +
                                            " | Apellido: " + usuario.getApellido() +
                                            " | Edad: " + usuario.getEdad());
                        }

                    });
        });

        binding.logoutBtn.setOnClickListener(view -> {
            AuthUI.getInstance().signOut(MainActivity.this)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
        });

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MainActivity2.class));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (snapshotListener != null)
            snapshotListener.remove();
    }
}