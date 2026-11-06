package fi.uutishetki

sealed class FeedEntry(val key: String, val published: Long) {
    class Article(val news: News) : FeedEntry("news:${news.id}", news.published ?: 0L)
    class Traffic(val item: NearbyTraffic) : FeedEntry("traffic:${item.notice.id}", item.notice.published)
}
fun mergeFeed(news: List<News>, traffic: List<NearbyTraffic>): List<FeedEntry> =
    (news.map { FeedEntry.Article(it) } + traffic.map { FeedEntry.Traffic(it) }).sortedByDescending { it.published }
