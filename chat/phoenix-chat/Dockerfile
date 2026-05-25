FROM elixir:1.19-alpine AS build

RUN apk add --no-cache build-base git

WORKDIR /app

ENV MIX_ENV=prod

RUN mix local.hex --force && mix local.rebar --force

COPY mix.exs ./
COPY config config
RUN mix deps.get --only prod && mix deps.compile

COPY lib lib
RUN mix compile
RUN mix release

FROM alpine:3.22

RUN apk add --no-cache libstdc++ ncurses-libs openssl

WORKDIR /app

ENV MIX_ENV=prod \
    PORT=4001

COPY --from=build /app/_build/prod/rel/chat_realtime ./

CMD ["bin/chat_realtime", "start"]
