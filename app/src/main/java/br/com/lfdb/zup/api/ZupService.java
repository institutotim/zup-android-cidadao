package br.com.lfdb.zup.api;

import br.com.lfdb.zup.api.model.ReportItem;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface ZupService {

    @POST("/reports/{categoryId}/items")
    ReportItem createReport(@Path("categoryId") long categoryId, @Body ReportItem item);
}
