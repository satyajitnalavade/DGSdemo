# DGSdemo
Demo project for demonstrating capabilities of DGS GraphQL framework by Netflix

Test the app with GraphiQL
Start the application and open a browser to http://localhost:8080/graphiql. GraphiQL is a query editor that comes out of the box with the DGS framework. 
Write the following query and tests the result.

{
    shows {
        title
        releaseYear
    }
}
Note that unlike with REST, you have to specifically list which fields you want to get returned from your query. 
This is where a lot of the power from GraphQL comes from, but a surprise to many developers new to GraphQL.
