package br.com.lfdb.zup.api;

import br.com.lfdb.zup.api.model.ReportItem;
import br.com.lfdb.zup.api.model.ReportItemRequest;
import br.com.lfdb.zup.api.model.ReportItemResponse;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface ZupService {

    @POST("/reports/{categoryId}/items")
    ReportItemResponse createReport(@Path("categoryId") long categoryId, @Body ReportItemRequest item);
}
