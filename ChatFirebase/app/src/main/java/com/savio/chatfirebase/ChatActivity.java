package com.savio.chatfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;


public class ChatActivity extends AppCompatActivity {
    private GroupAdapter adapter;
    private Usuario user;
    private Button btnEnviar;
    private EditText cxEditChat;
    private Usuario me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        RecyclerView rv = findViewById(R.id.recyler_chat);

        user = getIntent().getExtras().getParcelable("user"); // pega o objeto usser que a é a pessoa que a gente quer conversar
        btnEnviar =  findViewById(R.id.btn_chat);
        cxEditChat = findViewById(R.id.edit_chat);
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        getSupportActionBar().setTitle(user.getUser_name());
        adapter =  new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);


        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid()) // nós
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(Usuario.class); // passando a gente pra ca
                       fetchMessages();
                    }
                });

    }
    private void fetchMessages() {
        if (me != null) {

            String fromId = me.getUser_id();
            String toId = user.getUser_id();
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(fromId)
                    .collection(toId) // navegando dentro de colection indo até a conversa
                    .orderBy("timestamp", Query.Direction.ASCENDING)//mostrar como pilha baseado no timestamp
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                            if (documentChanges != null) {
                                for (DocumentChange doc: documentChanges) {

                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            Message message = doc.getDocument().toObject(Message.class);
                                            adapter.add(new MessageItem(message));
                                        }
                                }

                            }

                        }
                    });


        }
    }
    private void sendMessage(){
       String text = cxEditChat.getText().toString();
       cxEditChat.setText(null);

       String fromID = FirebaseAuth.getInstance().getUid();
       String toID = user.getUser_id();
       long timesstamp = System.currentTimeMillis();

       Message message = new Message();
        message.setFromId(fromID);
        message.setToID(toID);
        message.setTimestamp(timesstamp);
        message.setText(text);

       if(!message.getText().isEmpty()){
           FirebaseFirestore.getInstance().collection("/conversations") // vai criar/usar aqui as conversas gerais
           .document(fromID)// ID de quem enviou (eu)
           .collection(toID) //AQUI VAI CRIAR A *COLLECTION* DENTRO DA *COLLECTION CONVERSATION* QUE VAI SER A CONVERSA EFETIVAMENTE
           .add(message) //adiconando o objeito msg (com os ids, texto e o timestamp
           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
               @Override
               public void onSuccess(DocumentReference documentReference) {
                   Log.e("teste",documentReference.getId());

                    Contact contact = new Contact(); ///registrando ultima msg
                    contact.setUserID(toID);
                   contact.setUserName(user.getUser_name());
                    contact.setUrlProfilePicture(user.getUser_url_profilepicture());
                    contact.setTimestamp(message.getTimestamp());
                    contact.setLastMessage(message.getText());

                   FirebaseFirestore.getInstance().collection("/last-messages")
                           .document(fromID)
                            .collection("contacts")
                            .document(toID)
                            .set(contact);
                   if(!user.isOnline()){
                       Notification notification = new Notification();
                       notification.setFromId(message.getFromId());
                       notification.setToID(message.getToID());
                       notification.setFromName(me.getUser_name());
                       notification.setTimestamp(message.getTimestamp());
                       notification.setText(message.getText());

                       FirebaseFirestore.getInstance().collection("/notifications")
                               .document(me.getToken())
                                .set(notification)

                       ;

                   }

               }
           })
           .addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull  Exception e) {
                   Log.i("teste",e.getMessage());
               }
           })
           ;
           FirebaseFirestore.getInstance().collection("/conversations") // vai criar/usar aqui as conversas gerais
                   .document(toID)// ID de quem enviou (eu)
                   .collection(fromID) //AQUI VAI CRIAR A *COLLECTION* DENTRO DA *COLLECTION CONVERSATION* QUE VAI SER A CONVERSA EFETIVAMENTE
                   .add(message)
                   .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                       @Override
                       public void onSuccess(DocumentReference documentReference) {
                           Log.e("teste",documentReference.getId());
                           Contact contact = new Contact(); ///registrando ultima msg
                           contact.setUserID(toID);
                           contact.setUserName(user.getUser_name());
                           contact.setUrlProfilePicture(user.getUser_url_profilepicture());
                           contact.setTimestamp(message.getTimestamp());
                           contact.setLastMessage(message.getText());

                           FirebaseFirestore.getInstance().collection("/last-messages")
                                   .document(toID)
                                   .collection("contacts")
                                   .document(fromID)
                                   .set(contact);
                       }
                   })
                   .addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull  Exception e) {
                           Log.i("teste",e.getMessage());
                       }
                   })
           ;

       }
    }
    private class MessageItem extends Item<ViewHolder>{

      private final Message message;



        public MessageItem(Message message) {
            this.message = message;
        }


        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txtMensagem = viewHolder.getRoot().findViewById(R.id.txtMessage);
            ImageView imguserMessage = viewHolder.getRoot().findViewById(R.id.image_message_user);
            txtMensagem.setText(message.getText());
            Picasso.get()
                    .load(message.getFromId().equals(FirebaseAuth.getInstance().getUid()) ? // foi eu quem enviou?
                            me.getUser_url_profilepicture() // sim
                            :user.getUser_url_profilepicture()// nao
                    )
                    .into(imguserMessage);
        }


        @Override
        public int getLayout() {
            return message.getFromId().equals( FirebaseAuth.getInstance().getUid()) ?
                    R.layout.item_to_message
                    :R.layout.item_from_message; //operador ternario
        }
    }
}