package com.savio.chatfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextSenha, editTextUserName;
    private Button btnEnter,btnSelectedPhoto;
    private Uri imgSelected;
    private ImageView imgPhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUserName = findViewById(R.id.editUserName);
        editTextEmail = findViewById(R.id.editEmail);
        editTextSenha = findViewById(R.id.editPassword);
        btnEnter =  findViewById(R.id.btn_insert);
        btnSelectedPhoto = findViewById(R.id.btn_selected_photo);
        imgPhoto = findViewById(R.id.img_photo);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
        btnSelectedPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });
    }
    public void selectPhoto(){
        Intent intent =  new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 ){
           if(data != null){
               imgSelected = data.getData();

               Bitmap bitmap = null;
               try {
                   bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgSelected);
                   imgPhoto.setImageDrawable(new BitmapDrawable(bitmap));
                   btnSelectedPhoto.setAlpha(0);

               }catch (IOException e){

               }
           }

        }
    }

    private void createUser(){
        String nome =  editTextUserName.getText().toString();
      String email =  editTextEmail.getText().toString();
      String senha = editTextSenha.getText().toString();

      if((email ==  null) || (email.isEmpty()) || (senha == null) || (senha.isEmpty()) ||(nome == null) || (nome.isEmpty()) ){
          Toast.makeText(this,"Nome, senha e email devem ser inseridos",Toast.LENGTH_SHORT).show();
          return;
      }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha)// cria o usuario com email e senha APENAS ( fotos/nome e outras fodas vai ser salva no objeto usuairo)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.i("teste", task.getResult().getUser().getUid());
                    saveUserInFirebase(); // aí salva as outras infos do user
                }
                }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("teste",e.getMessage());
            }
        })
        ;

    }
    private void saveUserInFirebase(){
        String filename = UUID.randomUUID().toString();// gerar um nome aleatorio pra imagem pro upload
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/"+filename); // local no Storage onde a imagem vai ser guardada (se nao existir vai ser criada)
        ref.putFile(imgSelected)// vai salvar a imagem que foi salva (acho que do jeito que ta PODE dar um NUllpointerException
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { ///verificar se consegiu upar a imagem
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {// se deu bom faz o download da url da imagem pra usar agui e salvar no objeto USUARIO
                        @Override
                        public void onSuccess(Uri uri) {
                                Log.i("teste",uri.toString());
                                String user_id = FirebaseAuth.getInstance().getUid(); // JA ESTAMOS LOGADOS E PASSAMOS NOSSO ID PRA CA
                                String user_name = editTextUserName.getText().toString(); //salvar o nome
                                String urlphoto = uri.toString(); //salvar URL da foto
                                Usuario usuario = new Usuario(user_id,user_name,urlphoto); // criando OBJETO USUARIO

                            FirebaseFirestore.getInstance().collection("users") // memsma foda, vai criar se nao exisitir, mas dessa vez em COLLECTIONS

                                    .document(user_id) // fai colocar na referencia o ID do Usuario
                                    .set(usuario) //vai add o objeto na collection
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Intent intent =  new Intent(RegisterActivity.this,MessagesActivity.class);

                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // FAZ QUE SEJA  A login ACITIVITY PRINCIPAL
                                            startActivity(intent);

                                        }
                                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull  Exception e) {
                                    Log.i("teste", e.getMessage());
                                }
                            });

                        }
                    });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    Log.e("teste",e.getMessage());

            }
        });
    }
}
