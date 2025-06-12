package eu.infolead.jtk.rfc;

import java.net.MalformedURLException;
import java.net.URL;

import eu.infolead.jtk.fp.either.Result;

public record RfcReference(int number) {
    public static final String BASE_URL = "https://www.rfc-editor.org/rfc/rfc";

    public Result<Void, URL> jsonUrl() {
        try {
            return Result.success(new URL(BASE_URL + number() + ".json"));
        } catch (MalformedURLException e) {
            return Result.failure(/* TODO */null);
        }
    }

    public Result<Void, URL> textUrl() {
        try {
            return Result.success(new URL(BASE_URL + number() + ".txt"));
        } catch (MalformedURLException e) {
            return Result.failure(/* TODO */null);
        }
    }

    public Result<Void, URL> url() {
        try {
            return Result.success(new URL(BASE_URL + number()));
        } catch (MalformedURLException e) {
            return Result.failure(/* TODO */null);
        }
    }

    public Result<Void, URL> url(final String section) {
        // TODO validate section format
        try {
            return Result.success(new URL(BASE_URL + number() + "#section-" + section));
        } catch (MalformedURLException e) {
            return Result.failure(/* TODO */null);
        }
    }
}
