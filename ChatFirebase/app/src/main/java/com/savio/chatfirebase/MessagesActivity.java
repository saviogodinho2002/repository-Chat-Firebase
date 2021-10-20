package com.savio.chatfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.installations.FirebaseInstallations;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private RecyclerView rv;
    private GroupAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatAplication aplication = (ChatAplication) getApplication();
        getApplication().registerActivityLifecycleCallbacks(aplication);// MessagesActivity

        setContentView(R.layout.activity_messages);
        rv = findViewById(R.id.recycler_contacts);
        adapter = new GroupAdapter();

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        verifyAutentication();
        updateToken();
        fetchLastMessages();
    }
    private void updateToken(){

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser(); //pega eu
        mUser.getIdToken(true)//pega meu id
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() { //se conseguiu pega é gg
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String userID = FirebaseAuth.getInstance().getUid();
                           String token = task.getResult().getToken();
                            if(userID != null){
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(userID)
                                        .update("token",token)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                            }

                            Log.e("teste",token);

                        } else {


                        }
                    }
                });

    }
    private void fetchLastMessages(){
        String me_userID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("/last-messages")
                .document(me_userID)
                .collection("contacts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable  QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                       List<DocumentChange> documentChanges = value.getDocumentChanges();
                       if(documentChanges != null){

                           for (DocumentChange doc:documentChanges) {

                                  if(doc.getType() == DocumentChange.Type.ADDED){
                                      Contact contato = doc.getDocument().toObject(Contact.class);

                                      adapter.add(new ContactItem(contato));
                                  }
                           }
                       }
                    }
                });
    }
    protected void verifyAutentication(){
        if(FirebaseAuth.getInstance().getUid() ==  null) { // verifica se ta logado,  se nao tiver manda pra tela de Login
            Intent intent = new Intent(MessagesActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // FAZ QUE SEJA  A proxima ACITIVITY PRINCIPAL
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu,menu); // o menu que eu criei (menu xml) vai ser botado aqui
      return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // esse metodo que escuta as coisas que eu faço no menu
        switch (item.getItemId()){
            case R.id.contacts:
                Intent intent =  new Intent(MessagesActivity.this,ContactsActivity.class);
                Log.e("teste","PASSANDO PRA CONTATOS");
                startActivity(intent);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut(); // deslogo
                verifyAutentication();
                break;
        }
    return true;
    }
    private class ContactItem extends Item<ViewHolder>{
       private final Contact contato;

        public ContactItem(Contact contato) {
            this.contato = contato;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            ImageView profilePicture = viewHolder.getRoot().findViewById(R.id.imageView);
            TextView userName =  viewHolder.getRoot().findViewById(R.id.textView);
            TextView lastMessage =   viewHolder.getRoot().findViewById(R.id.textView2);
            userName.setText(contato.getUserName());
            lastMessage.setText(contato.getLastMessage());
            Picasso.get()
                    .load(contato.getUrlProfilePicture())
                    .into(profilePicture);
            ;
        }

        @Override
        public int getLayout() {
            return R.layout.item_user_message;
        }
    }
}