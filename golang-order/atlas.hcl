env "local" {
  url = "{{ env `DATABASE_URL` }}"
  dev-url = "{{ env `DATABASE_URL` }}"
  migration {
    dir = "file://migrations"
    format = "golang-migrate"
  }
}
