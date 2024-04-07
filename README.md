# urls

> Parses URLs and implements query escaping.

URL parser and builder, transplanted from `net/url` in Golang.

## Usage

Most of the APIs are the same as `net/url` in Golang, you can check out those usages in [url package - net/url - Go packages](https://pkg.go.dev/net/url#Values.Get).

But there is a difference in `Values`.
In `net/url`, you can use `func (Values) Get` to get the first value associated with the given key,
but in this `urls` tool, because I implemented `Values` by extending `HashMap`, I decide not to override the `get()` method,
so you can use the `get()` method to visit values associated with the given key just like the syntax `v["given_key"]` in Golang.

Accordingly, I added a `value()` method to allow you to get the first value in `Values` so you can use the `value()` method to do the same thing as the function `func (Values) Get` in Golang.

Also, there's a difference in `URL`.
In `net/url`, you can use `func (*URL) String` to get the valid URL string, but in this `urls` tool, I decide to override the `toString()` method to do the same thing.
So, the `toString()` method will not describe the `URL` object itself.
