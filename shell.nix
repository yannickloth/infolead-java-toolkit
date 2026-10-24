{ sources ? import ./nix/sources.nix
, pkgs ? import sources.nixpkgs {}
}:
pkgs.mkShell {
  buildInputs = with pkgs; [
    # keep this line if you use bash
    bashInteractive

    git
    google-java-format
    jdk
    maven
    niv
  ];
}
