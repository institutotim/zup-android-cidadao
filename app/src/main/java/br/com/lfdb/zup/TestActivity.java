package br.com.lfdb.zup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import br.com.lfdb.zup.util.ImageUtils;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final Adapter adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        adapter.addAll();

        findViewById(R.id.toggleAll).setOnClickListener(v -> {
            TextView text = (TextView) v.findViewById(R.id.textoTodos);
            ImageView icon = (ImageView) v.findViewById(R.id.iconeTodos);

            if (v.getTag() != null) {
                v.setTag(null);
                text.setText("Ativar todas as categorias");
                icon.setImageResource(R.drawable.filtros_check_todascategorias_ativar);
                adapter.addAll();
            } else {
                v.setTag(new Object());
                text.setText("Desativar todas as categorias");
                icon.setImageResource(R.drawable.filtros_check_todascategorias_desativar);
                adapter.removeAll();
            }
        });
    }

    public Activity getContext() {
        return this;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<CategoriaRelato> categories;
        private final Set<Long> expanded = new HashSet<>();
        private final Set<CategoriaRelato> selecionadas = new HashSet<>();

        public Adapter() {
            categories = new CategoriaRelatoService().getCategorias(getContext());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expandable_category, parent, false));
        }

        public void addAll() {
            for (int i = 0; i < categories.size(); i++) {
                selecionadas.add(categories.get(i));
                selecionadas.addAll(categories.get(i).getSubcategorias());
            }
            notifyDataSetChanged();
        }

        public void removeAll() {
            selecionadas.clear();
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final CategoriaRelato categoria = categories.get(position);

            holder.imagem.setImageBitmap(ImageUtils.getScaledCustom(getContext(), "reports", selecionadas.contains(categoria) ?
                    categoria.getIconeAtivo() : categoria.getIconeInativo(), 0.75f));
            holder.nomeCategoria.setText(categoria.getNome());
            holder.nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(categoria) ?
                    R.drawable.filtros_check_categoria : 0, 0);
            holder.expander.setOnClickListener(v -> {
                switchState(categoria.getId());
                checkExpanded(categoria, holder);
            });

            holder.nomeCategoria.setOnClickListener(v -> {
                if (selecionadas.contains(categoria)) {
                    selecionadas.remove(categoria);
                    selecionadas.removeAll(categoria.getSubcategorias());
                } else {
                    selecionadas.add(categoria);
                    selecionadas.addAll(categoria.getSubcategorias());
                }
                checkExpanded(categoria, holder);
                holder.nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(categoria) ?
                        R.drawable.filtros_check_categoria : 0, 0);
                holder.imagem.setImageBitmap(ImageUtils.getScaledCustom(getContext(), "reports", selecionadas.contains(categoria) ?
                        categoria.getIconeAtivo() : categoria.getIconeInativo(), 0.75f));
            });

            checkExpanded(categoria, holder);
        }

        protected void checkExpanded(CategoriaRelato categoria, ViewHolder holder) {
            if (holder.subcategorias.getChildCount() > 0) holder.subcategorias.removeAllViews();
            if (expanded.contains(categoria.getId())) {
                holder.expander.setText("Ocultar subcategorias");
                holder.subcategorias.setVisibility(View.VISIBLE);
                for (CategoriaRelato subcategory : categoria.getSubcategorias()) addSubcategoria(subcategory, holder.subcategorias);
            } else {
                holder.expander.setText("Ver subcategorias");
                holder.subcategorias.setVisibility(View.GONE);
            }
        }

        private void addSubcategoria(CategoriaRelato subcategory, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.item_subcategoria, parent, false);
            view.setTag(subcategory);
            final TextView nome = ButterKnife.findById(view, R.id.nome);
            nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(subcategory) ? R.drawable.filtros_check_categoria : 0, 0);
            nome.setText(subcategory.getNome());

            view.setOnClickListener(v -> {
                if (selecionadas.contains(subcategory)) {
                    selecionadas.remove(subcategory);
                } else {
                    selecionadas.add(subcategory);
                }
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(subcategory) ?
                        R.drawable.filtros_check_categoria : 0, 0);
            });

            parent.addView(view);
        }

        protected void switchState(long id) {
            if (expanded.contains(id)) expanded.remove(id);
            else expanded.add(id);
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imagem;
            ViewGroup subcategorias;
            TextView nomeCategoria;
            TextView expander;

            public ViewHolder(View view) {
                super(view);

                imagem = ButterKnife.findById(view, R.id.imagemCategoria);
                subcategorias = ButterKnife.findById(view, R.id.subcategorias);
                nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
                expander = ButterKnife.findById(view, R.id.expander);
            }
        }
    }
}
