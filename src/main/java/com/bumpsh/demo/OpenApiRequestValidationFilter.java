package com.bumpsh.demo;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.erosb.kappa.core.exception.ResolutionException;
import com.github.erosb.kappa.core.validation.ValidationException;
import com.github.erosb.kappa.operation.validator.adapters.server.servlet.JakartaServletRequest;
import com.github.erosb.kappa.operation.validator.adapters.server.servlet.MemoizingServletRequest;
import com.github.erosb.kappa.operation.validator.model.Request;
import com.github.erosb.kappa.operation.validator.validation.RequestValidator;
import com.github.erosb.kappa.parser.OpenApi3Parser;
import com.github.erosb.kappa.parser.model.v3.OpenApi3;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class OpenApiRequestValidationFilter implements Filter {
  // reading the OpenAPI description of our API
  private final OpenApi3 api;

    {
        try {
            api = new OpenApi3Parser().parse(getClass().getResource("/api/openapi.yaml"), false);
        } catch (ResolutionException | ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException {
    HttpServletRequest httpReq = (HttpServletRequest) req;
    HttpServletResponse httpResp = (HttpServletResponse) resp;

    try {

      // we need to wrap the original request instance into a MemoizingServletRequest,
      // since we will need to parse the request body twice: once for the OpenAPI-validation
      // and once for the jackson parsing.
      // basic HttpServletRequests cannot be read twice, hence we use the
      // MemoizingServletRequest shipped with Kappa
      // more here: https://www.baeldung.com/spring-reading-httpservletrequest-multiple-times
      MemoizingServletRequest memoizedReq = new MemoizingServletRequest(httpReq);

      // Kappa can understand different representations of HTTP requests and responses
      // here we use the Servlet API specific adapter of Kappa, to get a Kappa Request instance
      // which wraps a HttpServletRequest
      Request jakartaRequest = JakartaServletRequest.of(memoizedReq);

      // we do the validation
      new RequestValidator(api).validate(jakartaRequest);

      // if no request validation error was found, we proceed with the request execution
      chain.doFilter(memoizedReq, httpResp);

    } catch (ValidationException ex) {
      // if the request validation failed, we represents the validation failures in a simple
      // json response and send it back to the client
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode respObj = objectMapper.createObjectNode();
      ArrayNode itemsJson = objectMapper.createArrayNode();
      ex.results().forEach(item -> {
        ObjectNode itemJson = objectMapper.createObjectNode();
        itemJson.put("dataLocation", item.describeInstanceLocation());
        itemJson.put("schemaLocation", item.describeSchemaLocation());
        itemJson.put("message", item.getMessage());
        itemsJson.add(itemJson);
      });
      respObj.put("errors", itemsJson);
      httpResp.setStatus(400);
      httpResp.getWriter().print(objectMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(respObj)
      );
    } catch (ServletException e) {
        throw new RuntimeException(e);
    }
  }
}
