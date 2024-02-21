<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>BadArticleService Times</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/yegor256/tacit@gh-pages/tacit-css-1.6.0.min.css"/>
</head>

<body>

<h1>Список всех статей с комментариями</h1>
<table>
  <tr>
    <th>ID</th>
    <th>Title</th>
    <th>Tags</th>
    <th>Comments</th>
    <th>Trending status</th>
  </tr>
    <#list articles as article>
      <tr>
        <td>${article.id}</td>
        <td>${article.title}</td>
        <td>${article.tags}</td>
        <td>${article.comments}</td>
        <td>${article.trending}</td>
      </tr>
    </#list>
</table>
</body>
</html>