terraform {
  backend "s3" {
    bucket = "tanelso2-terraform-state"
    key    = "this.state"
    region = "us-east-2"
  }
}