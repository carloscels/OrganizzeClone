package com.example.cloneorganizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cloneorganizze.R;
import com.example.cloneorganizze.config.ConfiguracaoFirebase;
import com.example.cloneorganizze.helper.Base64Custom;
import com.example.cloneorganizze.helper.DataCustom;
import com.example.cloneorganizze.model.Movimentacao;
import com.example.cloneorganizze.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DespesasActivity extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        campoValor = findViewById(R.id.editValor);
        campoData = findViewById(R.id.editData);
        campoCategoria = findViewById(R.id.editCategoria);
        campoDescricao = findViewById(R.id.editDescricao);

        campoData.setText(DataCustom.dataAtual());

        recuperarDespesaTotal();

    }

    public void salvarDespesa(View view){
        if (validarCamposDespesas()){

            String data = campoData.getText().toString();
            movimentacao = new Movimentacao();
            Double valorRecuperado = Double.parseDouble(campoValor.getText().toString());
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria(campoCategoria.getText().toString());
            movimentacao.setDescricao(campoDescricao.getText().toString());
            movimentacao.setData(data);
            movimentacao.setTipo("d");

            Double despesaAtualizada = despesaTotal + valorRecuperado;
            atualizarDespesa(despesaAtualizada);

            movimentacao.salvar(data);
            finish();
        }
    }

    public boolean validarCamposDespesas(){

        String textoDescricao = campoDescricao.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();

        if (!textoValor.isEmpty()){
            if (!textoData.isEmpty()){
                if (!textoCategoria.isEmpty()){
                    if (!textoDescricao.isEmpty()){

                        return true;
                    }
                    else {
                        Toast.makeText(DespesasActivity.this,"Descrição não informado!",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                else {
                    Toast.makeText(DespesasActivity.this,"Categoria não informado!",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            else {
                Toast.makeText(DespesasActivity.this,"Data não informado!",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else {
            Toast.makeText(DespesasActivity.this,"Valor não informado!",Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public void recuperarDespesaTotal(){
        String emailUsuario =autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void atualizarDespesa(Double despesa){
        String emailUsuario =autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.child("despesaTotal").setValue(despesa);
    }
}
