package com.savio.chatfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    public GroupAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        RecyclerView rv = findViewById(R.id.recyler);
        adapter = new GroupAdapter();// cooisa que eu importei

         rv.setAdapter(adapter); // colocar as coisas do adapter no recycler view
         rv.setLayoutManager(new LinearLayoutManager(this)); // faz a foda funcionar

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull  Item item, @NonNull View view) {
                Intent intent =  new Intent(ContactsActivity.this,ChatActivity.class);
                UserItem userItem = (UserItem) item;
                intent.putExtra("user",userItem.user); //passar o usar da pra outra tela (ta pegando de dentro da subclass la em baixo)

                startActivity(intent);
            }
        });

           fetchUsers();


    }
    private void fetchUsers(){ //puxar os usuarios
        FirebaseFirestore.getInstance().collection("users") .addSnapshotListener(new EventListener<QuerySnapshot>() {// acho que depois de ja criado tem que colocar a '/'
            @Override
            public void onEvent(@Nullable  QuerySnapshot value, @Nullable  FirebaseFirestoreException error) {
                    if(error !=  null){
                        Log.e("teste",error.getMessage());
                        return;
                    }
            List<DocumentSnapshot> documentSnapshots = value.getDocuments(); //pega a lista de usuarios no servidor
                adapter.clear();
                for (DocumentSnapshot doc:documentSnapshots) {
                          Usuario user =  doc.toObject(Usuario.class); // passagem por valor com a pica do serivodr
                    Log.d("teste",user.getUser_name());

                    if(!user.getUser_id().equals(FirebaseAuth.getInstance().getUid()) )
                    adapter.add(new UserItem(user)); // adiciona ao adapter pra jogar pra interface
                }
            }
        });
    }
    private class UserItem extends Item<ViewHolder>  {
    private final Usuario user;
    public UserItem(Usuario user){

        //
        this.user = user;
    }

        @Override
        public void bind(ViewHolder viewHolder, int position) {
            TextView txtUserName = viewHolder.getRoot().findViewById(R.id.textView);
             ImageView imgUserProfile =  viewHolder.getRoot().findViewById(R.id.imageView);
             txtUserName.setText(user.getUser_name());

            Picasso.get() //facilita pegar as coisas (tme que importar)
                    .load(user.getUser_url_profilepicture()) // carrega para a url da imagem (em storage)...
                    .into(imgUserProfile); // .. aqui ...                                                                    me lembrou o LOAD do Assembly
        }

        @Override
        public int getLayout() {
            return R.layout.item_user; // passa o sexo que vai ser listado no caso aquele layout coma a imageview e textView
        }
    }
}