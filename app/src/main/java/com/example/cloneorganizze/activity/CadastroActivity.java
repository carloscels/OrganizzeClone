package com.example.cloneorganizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cloneorganizze.R;
import com.example.cloneorganizze.config.ConfiguracaoFirebase;
import com.example.cloneorganizze.helper.Base64Custom;
import com.example.cloneorganizze.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Button botaoCadastrar;

    private Usuario usuario;

    private FirebaseAuth Autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        //so consegui mudar o toolbar assim
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cor1)));

        campoNome = findViewById(R.id.editNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        botaoCadastrar = findViewById(R.id.botaoCadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if (!textoNome.isEmpty()){
                    if (!textoEmail.isEmpty()){
                        if (!textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);
                            cadastrarUsuario();
                        }
                        else {
                            Toast.makeText(CadastroActivity.this,"Preencha a Senha!",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(CadastroActivity.this,"Preencha a Email!",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(CadastroActivity.this,"Preencha o Nome!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void cadastrarUsuario(){
        Autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        Autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener( CadastroActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setIdUsuario(idUsuario);
                    usuario.salvar();
                    finish();
                }else {

                    String excecao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "email invalido!";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "esta conta ja foi cadastrada!";
                    }catch (Exception e){
                        excecao = "erro ao cadastrar usuario: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,excecao,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}