package br.com.lfdb.zup.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.lfdb.zup.R;
import br.com.lfdb.zup.domain.BuscaExplore;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class ReportCategoryAdapter extends RecyclerView.Adapter<ReportCategoryAdapter.ViewHolder> {

    private final List<CategoriaRelato> categories;
    private final Context context;
    private BuscaExplore filtro;

    public ReportCategoryAdapter(Context context, BuscaExplore filtro) {
        this.context = context;
        this.filtro = filtro;
        categories = new CategoriaRelatoService().getCategorias(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expandable_category, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoriaRelato categoria = categories.get(position);
        holder.nomeCategoria.setText(categoria.getNome());
        if (filtro.getIdsCategoriaRelato().contains(categoria.getId())) {
            Picasso.with(context).load(categoria.getIconeAtivo(context)).into(holder.imagem);
        } else {
            Picasso.with(context).load(categoria.getIconeInativo(context)).into(holder.imagem);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.imagemCategoria)
        ImageView imagem;
        @InjectView(R.id.nomeCategoria)
        TextView nomeCategoria;
        @InjectView(R.id.subcategorias)
        ViewGroup subcategorias;
        @InjectView(R.id.expander)
        TextView expander;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
