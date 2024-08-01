module "ecr" {
  source = "./modules/ecr/"

  for_each = var.ecr_repos

  name         = each.key
  mutable      = each.value.mutable
  scan_on_push = each.value.scan_on_push
  policy       = each.value.policy_file != null ? file(join("/", [".", "resources", "policies", each.value.policy_file])) : null
}
