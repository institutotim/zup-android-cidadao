package br.com.lfdb.zup.core;

import android.content.Context;

import br.com.lfdb.zup.util.ImageUtils;

public class ConstantesBase {

    public static String getItemInventarioQuery(Context context) {
        return new StringBuilder("?return_fields=")
                .append("id,")
                .append("position.latitude,")
                .append("position.longitude,")
                .append("inventory_category_id")
                .toString();
    }

    public static String getItemRelatoQuery(Context context) {
        String imageSize = ImageUtils.shouldDownloadRetinaIcon(context) ? "high" : "low";
        return new StringBuilder("?return_fields=")
                .append("id,")
                .append("description,")
                .append("protocol,")
                .append("address,")
                .append("created_at,")
                .append("category.id,")
                .append("position.latitude,")
                .append("position.longitude,")
                .append("inventory_item_id,")
                .append("status_id,")
                .append("reference,")
                .append("images,")
                .append("user.id")
                .toString();
    }

    public static String getCategoriasInventarioQuery(Context context) {
        String imageSize = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        return new StringBuilder("?return_fields=")
                .append("plot_format,")
                .append("icon,")
                .append("id,")
                .append("marker,")
                .append("pin,")
                .append("title")
                .toString();
    }

    public static String getCategoriasRelatoQuery(Context context) {
        String imageSize = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        return new StringBuilder("?return_fields=")
                .append("icon,")
                .append("marker,")
                .append("id,")
                .append("title,")
                .append("resolution_time,")
                .append("user_response_time,")
                .append("confidential,")
                .append("resolution_time_enabled,")
                .append("private_resolution_time,")
                .append("statuses.id,")
                .append("statuses.title,")
                .append("statuses.color,")
                .append("inventory_categories.id,")
                        // subcategories
                .append("subcategories.").append("icon,")
                .append("subcategories.").append("marker,")
                .append("subcategories.").append("id,")
                .append("subcategories.").append("title,")
                .append("subcategories.").append("resolution_time,")
                .append("subcategories.").append("user_response_time,")
                .append("subcategories.").append("confidential,")
                .append("subcategories.").append("resolution_time_enabled,")
                .append("subcategories.").append("private_resolution_time,")
                .append("subcategories.").append("statuses.id,")
                .append("subcategories.").append("statuses.title,")
                .append("subcategories.").append("statuses.color,")
                .append("subcategories.").append("inventory_categories.id")
                .toString();
    }
}
