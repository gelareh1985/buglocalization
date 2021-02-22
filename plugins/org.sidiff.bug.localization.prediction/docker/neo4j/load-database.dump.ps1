$script_path = split-path -parent $MyInvocation.MyCommand.Definition

$from_dump_filename = "neo4j-eclipse.jdt.core_test_samples-2021-02-14.dump"
$from_dump_folder = "${script_path}/backups"
$to_database_folder = "${script_path}/data"

docker run --interactive --tty --rm --publish=7474:7474 --publish=7687:7687 --volume=${to_database_folder}:/data --volume=${scriptPath}${from_dump_folder}:/backups neo4j:latest neo4j-admin load --from=/backups/${from_dump_filename} --database=neo4j --force