package com.example.cloneorganizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cloneorganizze.R;
import com.example.cloneorganizze.config.ConfiguracaoFirebase;
import com.example.cloneorganizze.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class LoginActivity extends AppCompatActivity {

    private EditText campoEmailEntrar, campoSenhaEntrar;
    private Button botaoEntrar;

    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //so consegui mudar o toolbar assim
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cor1)));

        campoEmailEntrar = findViewById(R.id.editEmailLogin);
        campoSenhaEntrar = findViewById(R.id.editSenhaLogin);
        botaoEntrar = findViewById(R.id.botaoEntrar);

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoEmail = campoEmailEntrar.getText().toString();
                String textoSenha = campoSenhaEntrar.getText().toString();

                if (!textoEmail.isEmpty()){
                    if (!textoSenha.isEmpty()){
                        usuario = new Usuario();
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);
                        validarLogin();

                    }
                    else {
                        Toast.makeText(LoginActivity.this,"Preencha a Senha!",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(LoginActivity.this,"Preencha a Email!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void  validarLogin(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    abrirTelaPrincipal();

                }else {
                    String excecao = "";
                    try{
                        throw task.getException();

                    }
                    catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuario não esta cadastrado.";
                    }
                    catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Senha não correspondente ao cadastrado!";
                    }
                    catch (Exception e){
                        excecao = "erro ao logar usuario: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this,excecao,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
        startActivity(intent);
        finish();
    }
}