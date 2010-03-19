package org.jboss.resteasy.specimpl;

import org.jboss.resteasy.util.Encode;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UriInfoImpl implements UriInfo
{
   private String path;
   private String encodedPath;
   private MultivaluedMap<String, String> queryParameters;
   private MultivaluedMap<String, String> encodedQueryParameters;
   private MultivaluedMap<String, String> pathParameters;
   private MultivaluedMap<String, String> encodedPathParameters;
   private MultivaluedMap<String, PathSegment[]> pathParameterPathSegments;
   private MultivaluedMap<String, PathSegment[]> encodedPathParameterPathSegments;

   private List<PathSegment> pathSegments;
   private List<PathSegment> encodedPathSegments;
   private URI absolutePath;
   private URI absolutePathWithQueryString;
   private URI baseURI;
   private List<String> matchedUris;
   private List<String> encodedMatchedUris;
   private List<Object> ancestors;


   public UriInfoImpl(URI absolutePath, URI baseUri, String encodedPath, String queryString, List<PathSegment> encodedPathSegments)
   {
      /*
      logger.info("**** URIINFO encodedPath: " + encodedPath);
      for (PathSegment segment : encodedPathSegments)
      {
         logger.info("   Segment: " + segment.getPath());
      }
      */
      this.encodedPath = encodedPath;
      this.path = Encode.decodePath(encodedPath);
      //System.out.println("path: " + path);
      //System.out.println("encodedPath: " + encodedPath);

      this.absolutePath = absolutePath;
      this.encodedPathSegments = encodedPathSegments;
      this.baseURI = baseUri;

      extractParameters(queryString);
      this.pathSegments = new ArrayList<PathSegment>(encodedPathSegments.size());
      for (PathSegment segment : encodedPathSegments)
      {
         pathSegments.add(new PathSegmentImpl(Encode.decodePath(((PathSegmentImpl) segment).getOriginal())));
      }
      if (queryString == null)
      {
         this.absolutePathWithQueryString = absolutePath;
      }
      else
      {
         this.absolutePathWithQueryString = URI.create(absolutePath.toString() + "?" + queryString);
      }
   }

   public String getPath()
   {
      return path;
   }

   public String getPath(boolean decode)
   {
      if (decode) return getPath();
      return encodedPath;
   }

   public List<PathSegment> getPathSegments()
   {
      if (pathSegments != null) return pathSegments;
      pathSegments = PathSegmentImpl.parseSegments(getPath());
      return pathSegments;
   }

   public List<PathSegment> getPathSegments(boolean decode)
   {
      if (decode) return getPathSegments();
      return encodedPathSegments;
   }

   public URI getRequestUri()
   {
      return absolutePathWithQueryString;
   }

   public UriBuilder getRequestUriBuilder()
   {
      return UriBuilder.fromUri(absolutePathWithQueryString);
   }

   public URI getAbsolutePath()
   {
      return absolutePath;
   }

   public UriBuilder getAbsolutePathBuilder()
   {
      return UriBuilder.fromUri(absolutePath);
   }

   public URI getBaseUri()
   {
      return baseURI;
   }

   public UriBuilder getBaseUriBuilder()
   {
      return UriBuilder.fromUri(baseURI);
   }

   public MultivaluedMap<String, String> getPathParameters()
   {
      if (pathParameters == null)
      {
         pathParameters = new MultivaluedMapImpl<String, String>();
      }
      return pathParameters;
   }

   public void addEncodedPathParameter(String name, String value)
   {
      getEncodedPathParameters().add(name, value);
      String value1 = Encode.decodePath(value);
      getPathParameters().add(name, value1);
   }

   private MultivaluedMap<String, String> getEncodedPathParameters()
   {
      if (encodedPathParameters == null)
      {
         encodedPathParameters = new MultivaluedMapImpl<String, String>();
      }
      return encodedPathParameters;
   }

   public MultivaluedMap<String, PathSegment[]> getEncodedPathParameterPathSegments()
   {
      if (encodedPathParameterPathSegments == null)
      {
         encodedPathParameterPathSegments = new MultivaluedMapImpl<String, PathSegment[]>();
      }
      return encodedPathParameterPathSegments;
   }

   public MultivaluedMap<String, PathSegment[]> getPathParameterPathSegments()
   {
      if (pathParameterPathSegments == null)
      {
         pathParameterPathSegments = new MultivaluedMapImpl<String, PathSegment[]>();
      }
      return pathParameterPathSegments;
   }

   public MultivaluedMap<String, String> getPathParameters(boolean decode)
   {
      if (decode) return getPathParameters();
      return getEncodedPathParameters();
   }

   public MultivaluedMap<String, String> getQueryParameters()
   {
      if (queryParameters == null)
      {
         queryParameters = new MultivaluedMapImpl<String, String>();
      }
      return queryParameters;
   }

   protected MultivaluedMap<String, String> getEncodedQueryParameters()
   {
      if (encodedQueryParameters == null)
      {
         this.encodedQueryParameters = new MultivaluedMapImpl<String, String>();
      }
      return encodedQueryParameters;
   }


   public MultivaluedMap<String, String> getQueryParameters(boolean decode)
   {
      if (decode) return getQueryParameters();
      else return getEncodedQueryParameters();
   }

   protected void extractParameters(String queryString)
   {
      if (queryString == null || queryString.equals("")) return;

      String[] params = queryString.split("&");

      for (String param : params)
      {
         if (param.indexOf('=') >= 0)
         {
            String[] nv = param.split("=");
            try
            {
               String name = URLDecoder.decode(nv[0], "UTF-8");
               String val = nv.length > 1 ? nv[1] : "";
               getEncodedQueryParameters().add(name, val);
               getQueryParameters().add(name, URLDecoder.decode(val, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
         else
         {
            try
            {
               String name = URLDecoder.decode(param, "UTF-8");
               getEncodedQueryParameters().add(name, "");
               getQueryParameters().add(name, "");
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
   }

   public List<String> getMatchedURIs(boolean decode)
   {
      if (decode)
      {
         if (matchedUris == null) matchedUris = new ArrayList<String>();
         return matchedUris;
      }
      else
      {
         if (encodedMatchedUris == null) encodedMatchedUris = new ArrayList<String>();
         return encodedMatchedUris;
      }
   }

   public List<String> getMatchedURIs()
   {
      return getMatchedURIs(true);
   }

   public List<Object> getMatchedResources()
   {
      if (ancestors == null) ancestors = new ArrayList<Object>();
      return ancestors;
   }


   public void pushCurrentResource(Object resource)
   {
      if (ancestors == null) ancestors = new ArrayList<Object>();
      ancestors.add(0, resource);
   }

   public void popCurrentResource()
   {
      if (ancestors != null && ancestors.size() > 0)
      {
         ancestors.remove(0);
      }
   }

   public void pushMatchedURI(String encoded, String decoded)
   {
      if (encodedMatchedUris == null) encodedMatchedUris = new ArrayList<String>();
      encodedMatchedUris.add(0, encoded);

      if (matchedUris == null) matchedUris = new ArrayList<String>();
      matchedUris.add(0, decoded);
   }

   public void popMatchedURI()
   {
      if (encodedMatchedUris != null && encodedMatchedUris.size() > 0)
      {
         encodedMatchedUris.remove(0);
      }
      if (matchedUris != null && matchedUris.size() > 0)
      {
         matchedUris.remove(0);
      }
   }

}
