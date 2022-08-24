package com.example.cloneorganizze.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.cloneorganizze.adapter.AdapterMovimentacao;
import com.example.cloneorganizze.config.ConfiguracaoFirebase;
import com.example.cloneorganizze.databinding.ActivityPrincipalBinding;
import com.example.cloneorganizze.helper.Base64Custom;
import com.example.cloneorganizze.model.Movimentacao;
import com.example.cloneorganizze.model.Usuario;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchUIUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cloneorganizze.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textoSaudacao, textoSaldo;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private Double despesaTotal =0.0;
    private Double receitaTotal =0.0;
    private Double resumoUsuario =0.0;

    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;

    private Movimentacao movimentacao;

    private RecyclerView recyclerView;
    private AdapterMovimentacao adapterMovimentacao;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private DatabaseReference movimentacaoRef;
    private String mesAnoSelecionado;


    private ActivityPrincipalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Organizze");
        setSupportActionBar(binding.toolbar);


        calendarView = findViewById(R.id.calendarView);
        textoSaudacao = findViewById(R.id.textSaudacao);
        textoSaldo = findViewById(R.id.textSaldo);
        recyclerView = findViewById(R.id.recyclerMovimentos);
        configuraCaldendarView();
        swipe();

        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterMovimentacao);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    public void swipe(){
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao(viewHolder);
            }
        };
        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerView);
    }

    public void excluirMovimentacao(RecyclerView.ViewHolder viewHolder){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this);
        alertDialog.setTitle("Excluir essa movimentação");
        alertDialog.setMessage("Você tem certeza que deseja excluir essa conta?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confimar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get(position);
                String emailUsuario =autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                movimentacaoRef=firebaseRef.child("movimentacao")
                        .child(idUsuario)
                        .child(mesAnoSelecionado);

                movimentacaoRef.child(movimentacao.getKey()).removeValue();
                adapterMovimentacao.notifyItemRemoved(position);
                atualizarSaldo();


            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(PrincipalActivity.this,
                        "Cancelado!",Toast.LENGTH_SHORT).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

    }

    public void atualizarSaldo(){
        String emailUsuario =autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        if (movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal-movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(receitaTotal);
        }
        if (movimentacao.getTipo().equals("d")){
            despesaTotal = despesaTotal-movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue(despesaTotal);
        }
    }

    public void recuperarMovimentacoes(){
        String emailUsuario =autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        movimentacaoRef=firebaseRef.child("movimentacao")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movimentacoes.clear();
                for (DataSnapshot dados: snapshot.getChildren()){
                    Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                    movimentacao.setKey(dados.getKey());
                    movimentacoes.add(movimentacao);
                }
                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void recuperarResumo(){
        String emailUsuario =autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                receitaTotal = usuario.getReceitaTotal();
                despesaTotal = usuario.getDespesaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String resultadoFormatado = decimalFormat.format(resumoUsuario);

                textoSaudacao.setText("Olá, "+ usuario.getNome());
                textoSaldo.setText("R$:" + resultadoFormatado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair:
            autenticacao.signOut();
            startActivity(new Intent(this,MainActivity.class));
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void adicionarDespesa(View view){
        Intent intent = new Intent(PrincipalActivity.this,DespesasActivity.class);
        startActivity(intent);
    }

    public void adicionarReceita(View view){
        Intent intent = new Intent(PrincipalActivity.this,ReceitasActivity.class);
        startActivity(intent);
    }

    public void configuraCaldendarView(){
        CharSequence meses[] = {"janeiro","fevereiro","março","abril","maio","junho","julho","agosto","setembro","outubro","novembro","dezembro"};
        calendarView.setTitleMonths( meses );

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d",(dataAtual.getMonth()+1));
        mesAnoSelecionado = String.valueOf( mesSelecionado +""+ dataAtual.getYear());

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String dateSelecionado = String.format("%02d",(date.getMonth()+1));
                mesAnoSelecionado = String.valueOf(dateSelecionado +""+ date.getYear());
                movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
                recuperarMovimentacoes();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerUsuario);
        movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
    }
}